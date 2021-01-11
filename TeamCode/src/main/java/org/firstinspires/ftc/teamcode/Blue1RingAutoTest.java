package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SelectCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.command.WaitUntilCommand;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.hardware.ServoEx;
import com.arcrobotics.ftclib.hardware.SimpleServo;
import com.arcrobotics.ftclib.hardware.motors.CRServo;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.arcrobotics.ftclib.vision.UGRectDetector;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.commands.GoToLineShootPowershotBlue;
import org.firstinspires.ftc.teamcode.commands.PlaceWobbleGoal;
import org.firstinspires.ftc.teamcode.commands.RunCommand;
import org.firstinspires.ftc.teamcode.commands.drive.TrajectoryFollowerCommand;
import org.firstinspires.ftc.teamcode.commands.drive.TurnCommand;
import org.firstinspires.ftc.teamcode.commands.shooter.ShootRingsCommand;
import org.firstinspires.ftc.teamcode.drive.SampleTankDrive;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.ShooterFeeder;
import org.firstinspires.ftc.teamcode.subsystems.ShooterWheels;
import org.firstinspires.ftc.teamcode.subsystems.Vision;
import org.firstinspires.ftc.teamcode.subsystems.WobbleGoalArm;

import java.util.HashMap;

@Autonomous(name = "1 Ring Blue")
public class Blue1RingAutoTest extends CommandOpMode {
    // Motors
    private MotorEx leftBackDriveMotor, rightBackDriveMotor, leftFrontDriveMotor, rightFrontDriveMotor;
    private MotorEx intakeMotor;
    private MotorEx anglerMotor;
    private DcMotorEx shooterMotorFront, shooterMotorBack;
    private CRServo arm;
    private ServoEx feedServo, clawServo;

    private ServoEx releaseShooter;
    // Gamepad
    private GamepadEx driverGamepad;

    // Subsystems
    private Drivetrain drivetrain;
    private ShooterWheels shooterWheels;
    private ShooterFeeder feeder;
    private Intake intake;
    private WobbleGoalArm wobbleGoalArm;
    private Vision vision;
    FtcDashboard dashboard = FtcDashboard.getInstance();
    Telemetry dashboardTelemetry = dashboard.getTelemetry();

    @Override
    public void initialize() {

        // Drivetrain Hardware Initializations
        // Intake hardware Initializations
        intakeMotor = new MotorEx(hardwareMap, "intake");

        // Shooter hardware initializations
        shooterMotorBack = (DcMotorEx) hardwareMap.get(DcMotor.class, "shooter_back");
        shooterMotorFront = (DcMotorEx) hardwareMap.get(DcMotor.class, "shooter_front");
        anglerMotor = new MotorEx(hardwareMap, "angler");
        feedServo = new SimpleServo(hardwareMap, "feed_servo", 0, 230);

        // Wobble Harware initializations
        arm = new CRServo(hardwareMap, "arm");
        clawServo = new SimpleServo(hardwareMap, "claw_servo", 0, 230);

        releaseShooter = new SimpleServo(hardwareMap, "release_servo", 0, 180);

        // Subsystems
        drivetrain = new Drivetrain(new SampleTankDrive(hardwareMap), telemetry);
        // intake = new Intake(intakeMotor, telemetry);
        shooterWheels = new ShooterWheels(shooterMotorFront, shooterMotorBack, telemetry);
        feeder = new ShooterFeeder(feedServo, telemetry);
        wobbleGoalArm = new WobbleGoalArm(arm, clawServo, telemetry);
        vision = new Vision(hardwareMap, "webcam", telemetry);


        Trajectory trajectory = drivetrain.trajectoryBuilder(new Pose2d())
                .back(48)
                .build();

        vision.getCurrentStack();
        // Gamepad
        schedule(new RunCommand(() -> {
            telemetry.addData("Stack", vision.getCurrentStack());
            telemetry.update();
        }));

        while (!isStarted() && !isStopRequested()) {
            telemetry.addData("Current Stack", vision.getCurrentStack());
            telemetry.update();
        }

        schedule(new WaitUntilCommand(this::isStarted).andThen(
                new InstantCommand(() -> releaseShooter.setPosition(0.2)),
                new SequentialCommandGroup(
                        new GoToLineShootPowershotBlue(drivetrain, shooterWheels, feeder),
                        new TurnCommand(drivetrain, -5.5),
                        new ShootRingsCommand(shooterWheels, feeder, 2600, 1),
                        new TrajectoryFollowerCommand(drivetrain, drivetrain.trajectoryBuilder(new Pose2d()).back(24).build()),
                        new PlaceWobbleGoal(wobbleGoalArm),
                        new InstantCommand(() -> drivetrain.setPoseEstimate(new Pose2d())),
                        new TrajectoryFollowerCommand(drivetrain, drivetrain.trajectoryBuilder(new Pose2d()).forward(24).build()),
                        new WaitCommand(1000)
                )
        ));

    }
}