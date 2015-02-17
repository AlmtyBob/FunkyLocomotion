package com.rwtema.funkylocomotion.movers;

import com.rwtema.funkylocomotion.helper.WeakSet;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;

public class MoverEventHandler {
    public static final WeakSet<IMover> movers = new WeakSet<IMover>();
    private static boolean skip = true;

    public static void init() {
        FMLCommonHandler.instance().bus().register(new MoverEventHandler());
    }

    public static void registerMover(IMover mover) {
        movers.add(mover);
        skip = false;
    }

    private MoverEventHandler() {
    }

    @SubscribeEvent
    public void onPostWorldTick(TickEvent.WorldTickEvent event) {
        if (skip && event.phase != TickEvent.Phase.END || event.side != Side.SERVER || movers.isEmpty())
            return;


        IMover[] iMovers = movers.toArray(new IMover[movers.size()]);
        movers.clear();

        for (IMover mover : iMovers) {
            if (mover.stillExists()) {
                mover.startMoving();
            }
        }

        movers.clear();
        skip = true;
    }
}
