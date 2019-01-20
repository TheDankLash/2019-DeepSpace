package com.spartronics4915.frc2019.subsystems;

import com.spartronics4915.lib.util.ILoop;
import com.spartronics4915.lib.util.ILooper;

//import com.spartronics4915.frc2019.Robot;

public class PanelHandler extends Subsystem
{

    private static PanelHandler mInstance = null;

    public static PanelHandler getInstance()
    {
        if (mInstance == null)
        {
            mInstance = new PanelHandler();
        }
        return mInstance;
    }

    public enum WantedState
    {
        DEACTIVATED, EJECT,
    }

    private enum SystemState
    {
        DEACTIVATING, EJECTING,
    }

    private WantedState mWantedState = WantedState.DEACTIVATED;
    private SystemState mSystemState = SystemState.DEACTIVATING;

    private PanelHandler()
    {
        boolean success = true;
        try
        {
            // Instantiate your hardware here
        }
        catch (Exception e)
        {
            success = false;
            logException("Couldn't instantiate hardware", e);
        }

        logInitialized(success);
    }

    private final ILoop mLoop = new ILoop()
    {

        @Override
        public void onStart(double timestamp)
        {
            synchronized (PanelHandler.this)
            {
                //mWantedState = WantedState.DEACTIVATED;
                mWantedState = WantedState.EJECT;
                mSystemState = SystemState.DEACTIVATING;
            }
        }

        @Override
        public void onLoop(double timestamp)
        {
            synchronized (PanelHandler.this)
            {
                outputTelemetry();
                SystemState newState = defaultStateTransfer();
                switch (mSystemState)
                {
                    case EJECTING:
                        break;
                    case DEACTIVATING:
                        stop();
                        break;
                    default:
                        logError("Unhandled system state!");
                }
                mSystemState = newState;
            }
        }

        @Override
        public void onStop(double timestamp)
        {
            synchronized (PanelHandler.this)
            {
                stop();
            }
        }
    };

    private SystemState defaultStateTransfer()
    {
        SystemState newState = mSystemState;
        switch (mWantedState)
        {
            case DEACTIVATED:
                if(mWantedState == WantedState.EJECT)
                    newState = SystemState.EJECTING;
                else
                    newState = SystemState.DEACTIVATING;
                    break;
            case EJECT:
                if(mWantedState == WantedState.DEACTIVATED)
                    newState = SystemState.DEACTIVATING;
                else
                    newState = SystemState.EJECTING;
                    break;
            default:
                newState = SystemState.DEACTIVATING;
                break;
        }
        return newState;
    }

    public synchronized void setWantedState(WantedState wantedState)
    {
        mWantedState = wantedState;
    }

    public synchronized boolean atTarget()
    {
        return true;
    }

    @Override
    public void registerEnabledLoops(ILooper enabledLooper)
    {
        enabledLooper.register(mLoop);
    }

    @Override
    public boolean checkSystem(String variant)
    {
        return false;
    }

    @Override
    public void outputTelemetry()
    {
        dashboardPutState(mSystemState.toString());
        dashboardPutWantedState(mWantedState.toString());
    }

    @Override
    public void stop()
    {
        // Stop your hardware here
    }
}
