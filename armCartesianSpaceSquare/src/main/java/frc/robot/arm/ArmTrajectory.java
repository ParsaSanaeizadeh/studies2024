package frc.robot.arm;

import frc.robot.Robot;
import frc.robot.armMotion.ArmAngles;

import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.Trajectory.State;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class ArmTrajectory extends CommandBase {
    public static class Config {
        public double oscillatorFrequencyHz = 2;
        /** amplitude (each way) of oscillation in encoder units */
        public double oscillatorScale = 0.025;
        /** start oscillating when this close to the target. */
        public double oscillatorZone = 0.1;
        public TrajectoryConfig safeTrajectory = new TrajectoryConfig(9, 1.5);
        public TrajectoryConfig normalTrajectory = new TrajectoryConfig(12, 2);
    }
    private final Config m_config = new Config();
    private final Robot m_robot;
    // private final ArmPosition m_position;
    // private final boolean m_oscillate;

    private final Timer m_timer;

    private final DoublePublisher measurmentX;
    private final DoublePublisher measurmentY;
    private final DoublePublisher setpointUpper;
    private final DoublePublisher setpointLower;

    private Trajectory m_trajectory;

    /**
     * Go to the specified position and optionally oscillate when you get there.
     */
    public ArmTrajectory(Robot robot) {
        m_robot = robot;
        // m_position = position;
        // m_oscillate = oscillate;
        m_timer = new Timer();

        NetworkTableInstance inst = NetworkTableInstance.getDefault();
        measurmentX = inst.getTable("Arm Trajec").getDoubleTopic("measurmentX").publish();
        measurmentY = inst.getTable("Arm Trajec").getDoubleTopic("measurmentY").publish();
        setpointUpper = inst.getTable("Arm Trajec").getDoubleTopic("Setpoint Upper").publish();
        setpointLower = inst.getTable("Arm Trajec").getDoubleTopic("Setpoint Lower").publish();

        // addRequirements(m_robot);
    }

    @Override
    public void initialize() {
        m_timer.restart();
        final TrajectoryConfig trajectoryConfig;
        // if (m_position == ArmPosition.SAFE) {
        //     trajectoryConfig = m_config.safeTrajectory;
        //     m_arm.setControlSafe();
        // } else {
            trajectoryConfig = m_config.normalTrajectory;
            // m_arm.setControlNormal();
        // }
        System.out.println("INTITIAIAKRIEWIERWRIEWERWI");
        m_trajectory = new ArmTrajectories(trajectoryConfig).makeTrajectory(
                m_robot.getMeasurement());
    }

    public void execute() {
        if (m_trajectory == null) {
            // System.out.println("Null sad");
            return;
        }
        System.out.println("WEORKKRRKKKKKKKKKKKKKKKKKKKKKKKK");
        ArmAngles measurement = m_robot.getMeasurement();
        double currentUpper = measurement.th2;
        double currentLower = measurement.th1;

        double curTime = m_timer.get();
        State desiredState = m_trajectory.sample(curTime);

        double desiredUpper = desiredState.poseMeters.getX();
        double desiredLower = desiredState.poseMeters.getY();

        // double upperError = desiredUpper - currentUpper;

        // if (m_oscillate && upperError < m_config.oscillatorZone) {
        //     desiredUpper += oscillator(curTime);
        // }

        ArmAngles reference = new ArmAngles(desiredLower, desiredUpper);

        m_robot.setReference(reference);

        measurmentX.set(currentUpper);
        measurmentY.set(currentLower);
        setpointUpper.set(desiredUpper);
        setpointLower.set(desiredLower);
    }

    @Override
    public void end(boolean interrupted) {
        // m_arm.setControlNormal();
        m_robot.setReference(m_robot.getMeasurement());
    }

    @Override
    public boolean isFinished() {
        // if (m_position == ArmPosition.SAFEWAYPOINT) {
        //     return m_timer.hasElapsed(m_trajectory.getTotalTimeSeconds());
        // }
        return false;
    }

    // private double oscillator(double timeSec) {
    //     return m_config.oscillatorScale * Math.sin(2 * Math.PI * m_config.oscillatorFrequencyHz * timeSec);
    // }

}
