export const API = {
  base: import.meta.env.VITE_API_BASE as string,
};

function isValidObjectId(id: string): boolean {
  return /^[a-f\d]{24}$/i.test(id);
}

async function http(path: string, opts: RequestInit = {}) {
  const res = await fetch(API.base + path, {
    headers: { "Content-Type": "application/json" },
    ...opts,
  });
  if (!res.ok) throw new Error(`HTTP ${res.status} ${res.statusText}`);
  if (res.status === 204) return null;
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
};
