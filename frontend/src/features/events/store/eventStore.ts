import { defineStore } from 'pinia'
import { EventService } from '../api/EventService'
import type { EventCreatedRes, EventView } from '../../../core/types'

export const useEventStore = defineStore('event', {
  state: () => ({
    current: null as EventView | null,
    inviteLinks: {} as Record<string,string>,
    loading: false as boolean,
    error: '' as string,
  }),
  actions: {
    async create(payload: { title:string; description?:string; place?:string; startAt?:string; endAt?:string; invites:string[] }): Promise<EventCreatedRes>{
      this.error=''; this.loading=true
      try{ return await EventService.create(payload) }
      catch(e:any){ this.error = e.message; throw e }
      finally{ this.loading=false }
    },
    async load(eventId: string){
      this.error=''
      try{ this.current = await EventService.get(eventId) }
      catch(e:any){ this.error = e.message }
    },
    async loadInviteLinks(eventId: string, sig: string){
      this.error=''
      try{ this.inviteLinks = await EventService.listInviteLinks(eventId, sig) }
      catch(e:any){ this.error = e.message }
    }
  }
})
