export const API = {
  base: import.meta.env.VITE_API_BASE as string,
};

function isValidObjectId(id: string): boolean {
  return /^[a-f\d]{24}$/i.test(id);
}

async function http(path: string, opts: RequestInit = {}) {
  const hasBody = !!(opts as any).body;
  const headers = new Headers(opts.headers || {});
  if (hasBody && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json"); // nécessaire pour POST/PATCH
  }
  // Pas de cookies -> évite CORS credentials
  const res = await fetch(API.base + path, {
    method: opts.method || "GET",
    mode: "cors",
    credentials: "omit",
    headers,
    body: (opts as any).body,
  });

  if (res.status === 204) return null;
  if (!res.ok) {
    // Essaie de remonter un message d'erreur lisible
    let msg: string;
    try {
      msg = (await res.text()) || `${res.status} ${res.statusText}`;
    } catch {
      msg = `${res.status} ${res.statusText}`;
    }
    throw new Error(`HTTP ${msg}`);
  }
  return res.json();
}
export const EventApi = {
  create(payload: {
    title: string;
    description?: string;
    place?: string;
    startAt?: string;
    endAt?: string;
    invites: string[];
  }) {
    return http("/events", { method: "POST", body: JSON.stringify(payload) });
  },
  get(eventId: string) {
    if (!isValidObjectId(eventId)) {
      throw new Error(`Invalid ObjectId: ${eventId}`);
    }
    return http(`/events/${eventId}`);
  },
  addBet(eventId: string, sig: string, text: string) {
    return http(`/events/${eventId}/bets?sig=${encodeURIComponent(sig)}`, {
      method: "POST",
      body: JSON.stringify({ text }),
    });
  },
  setBetStatus(
    eventId: string,
    betId: string,
    sig: string,
    status: "open" | "true" | "false"
  ) {
    return http(
      `/events/${eventId}/bets/${betId}?sig=${encodeURIComponent(sig)}`,
      {
        method: "PATCH",
        body: JSON.stringify({ status }),
      }
    );
  },
  getAsInvite(eventId: string, inviteId: string, sig: string) {
    return http(
      `/events/${eventId}/as-invite?inviteId=${inviteId}&sig=${encodeURIComponent(
        sig
      )}`
    );
  },
  predict(
    eventId: string,
    betId: string,
    inviteId: string,
    sig: string,
    choice: "Oui" | "Non"
  ) {
    return http(
      `/events/${eventId}/bets/${betId}/predict?inviteId=${inviteId}&sig=${encodeURIComponent(
        sig
      )}`,
      {
        method: "POST",
        body: JSON.stringify({ choice }),
      }
    );
  },
  listInviteLinks(eventId: string, sig: string) {
    return http(
      `/events/${eventId}/invites/links?sig=${encodeURIComponent(sig)}`
    );
  },
  matrix(eventId: string, sig: string) {
    return http(
      `/events/${eventId}/predictions/matrix?sig=${encodeURIComponent(sig)}`
    );
  },
  deleteBet(eventId: string, betId: string, sig: string) {
    return http(
      `/events/${eventId}/bets/${betId}?sig=${encodeURIComponent(sig)}`,
      { method: "DELETE" }
    );
  },
  addInvite(eventId: string, sig: string, name: string) {
    return http(`/events/${eventId}/invites?sig=${encodeURIComponent(sig)}`, {
      method: "POST",
      body: JSON.stringify({ name }),
    });
  },

  revokeInvite(eventId: string, inviteId: string, sig: string) {
    return http(
      `/events/${eventId}/invites/${inviteId}/revoke?sig=${encodeURIComponent(
        sig
      )}`,
      { method: "PATCH" }
    );
  },

  approveInvite(eventId: string, inviteId: string, sig: string) {
    return http(
      `/events/${eventId}/invites/${inviteId}/approve?sig=${encodeURIComponent(
        sig
      )}`,
      { method: "PATCH" }
    );
  },
  saveMyEvent(eventId: string, hostLink: string, sig: string) {
    const events = JSON.parse(localStorage.getItem("myEvents") || "[]");
    if (!events.find((e: any) => e.eventId === eventId)) {
      events.push({ eventId, hostLink, sig });
      localStorage.setItem("myEvents", JSON.stringify(events));
    }
  },
  saveMyProno(eventId: string, inviteId: string, sig: string) {
    const pronos = JSON.parse(localStorage.getItem("myPronos") || "[]");
    if (
      !pronos.find((p: any) => p.eventId === eventId && p.inviteId === inviteId)
    ) {
      pronos.push({ eventId, inviteId, sig });
      localStorage.setItem("myPronos", JSON.stringify(pronos));
    }
  },
};
