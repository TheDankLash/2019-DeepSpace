package com.spartronics4915.frc2019.subsystems;

import com.spartronics4915.frc2019.Constants;
import com.spartronics4915.frc2019.Kinematics;
import com.spartronics4915.frc2019.RobotState;
import com.spartronics4915.lib.util.ILooper;
import com.spartronics4915.lib.util.ILoop;
import com.spartronics4915.lib.geometry.Rotation2d;
import com.spartronics4915.lib.geometry.Twist2d;
import com.spartronics4915.lib.lidar.LidarProcessor;

public class RobotStateEstimator extends Subsystem
{

    static RobotStateEstimator instance_ = new RobotStateEstimator();
    public static RobotStateEstimator getInstance()
    {
        return instance_;
    }

    private RobotState robot_state_ = RobotState.getInstance();
    private Drive drive_ = Drive.getInstance();
    private double left_encoder_prev_distance_ = 0.0;
    private double right_encoder_prev_distance_ = 0.0;
    private double back_encoder_prev_distance_ = 0.0;
    private LidarProcessor mLidarProcessor;

    RobotStateEstimator()
    {
        /* warning, this starts up the lidar server, might need to
         * defer this til start.
         */
        mLidarProcessor = new LidarProcessor(LidarProcessor.RunMode.kRunInRobot, 
                            Constants.kSegmentReferenceModel,
                            robot_state_/*IPose2dMap*/);
    }

    @Override
    public boolean checkSystem()
    {
        return false;
    }

    @Override
    public void outputTelemetry()
    {
        // No-op
    }

    @Override
    public void stop()
    {
        // No-op
    }

    @Override
    public void registerEnabledLoops(ILooper looper)
    {
        looper.register(new EnabledLoop());
        looper.register(mLidarProcessor);
    }

    private class EnabledLoop implements ILoop
    {

        @Override
        public synchronized void onStart(double timestamp)
        {
            left_encoder_prev_distance_ = drive_.getLeftEncoderDistance();
            right_encoder_prev_distance_ = drive_.getRightEncoderDistance();

        }

        @Override
        public synchronized void onLoop(double timestamp)
        {
            final double left_distance = drive_.getLeftEncoderDistance();
            final double right_distance = drive_.getRightEncoderDistance();
            final double delta_left = left_distance - left_encoder_prev_distance_;
            final double delta_right = right_distance - right_encoder_prev_distance_;
            final Rotation2d gyro_angle = drive_.getHeading();
            final Twist2d odometry_velocity = robot_state_.generateOdometryFromSensors(
                    delta_left, delta_right, gyro_angle);
            final Twist2d predicted_velocity = Kinematics.forwardKinematics(drive_.getLeftLinearVelocity(),
                    drive_.getRightLinearVelocity());
            robot_state_.addObservations(timestamp, odometry_velocity,
                    predicted_velocity);
            left_encoder_prev_distance_ = left_distance;
            right_encoder_prev_distance_ = right_distance;
        }

        @Override
        public void onStop(double timestamp)
        {
            // no-op
        }
    }
}
