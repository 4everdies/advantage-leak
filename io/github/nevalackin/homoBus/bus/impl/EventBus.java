/*
 * Decompiled with CFR 0.152.
 */
package io.github.nevalackin.homoBus.bus.impl;

import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import io.github.nevalackin.homoBus.bus.Bus;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EventBus<Event>
implements Bus<Event> {
    private final Map<Type, List<CallSite<Event>>> callSiteMap = new HashMap<Type, List<CallSite<Event>>>();
    private final Map<Type, List<Listener<Event>>> listenerCache = new HashMap<Type, List<Listener<Event>>>();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    @Override
    public void subscribe(Object subscriber) {
        for (Field field : subscriber.getClass().getDeclaredFields()) {
            EventLink annotation = field.getAnnotation(EventLink.class);
            if (annotation == null) continue;
            Type eventType = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            try {
                List<Object> callSites;
                Listener listener = (Listener)LOOKUP.unreflectGetter(field).invokeWithArguments(subscriber);
                byte priority = annotation.value();
                CallSite callSite = new CallSite(subscriber, listener, priority);
                if (this.callSiteMap.containsKey(eventType)) {
                    callSites = this.callSiteMap.get(eventType);
                    callSites.add(callSite);
                    callSites.sort(Comparator.comparingInt(o -> o.priority));
                    continue;
                }
                callSites = new ArrayList(1);
                callSites.add(callSite);
                this.callSiteMap.put(eventType, callSites);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        this.populateListenerCache();
    }

    private void populateListenerCache() {
        Map<Type, List<CallSite<Event>>> callSiteMap = this.callSiteMap;
        Map<Type, List<Listener<Event>>> listenerCache = this.listenerCache;
        for (Type type : callSiteMap.keySet()) {
            List<CallSite<Event>> callSites = callSiteMap.get(type);
            int size = callSites.size();
            ArrayList listeners = new ArrayList(size);
            for (int i = 0; i < size; ++i) {
                listeners.add(callSites.get((int)i).listener);
            }
            listenerCache.put(type, listeners);
        }
    }

    @Override
    public void unsubscribe(Object subscriber) {
        for (List<CallSite<Event>> callSites : this.callSiteMap.values()) {
            callSites.removeIf(eventCallSite -> eventCallSite.owner == subscriber);
        }
        this.populateListenerCache();
    }

    @Override
    public void post(Event event) {
        List listeners = this.listenerCache.getOrDefault(event.getClass(), Collections.emptyList());
        int listenersSize = listeners.size();
        while (listenersSize > 0) {
            ((Listener)listeners.get(--listenersSize)).call(event);
        }
    }

    private static class CallSite<Event> {
        private final Object owner;
        private final Listener<Event> listener;
        private final byte priority;

        public CallSite(Object owner, Listener<Event> listener, byte priority) {
            this.owner = owner;
            this.listener = listener;
            this.priority = priority;
        }
    }
}

