package wrsn;

import java.util.*;

import io.jbotsim.core.*;
import io.jbotsim.ui.icons.Icons;

public class BaseStation extends Node {

	ArrayList<Sensor> sensors;
	int idRobot = -1;

	@Override
	public void onStart() {
		setIcon(Icons.STATION);
		setIconSize(16);
		sensors = getSensors();
		// Initiates tree construction with an empty message
		sendAll(new Message(null, "INIT"));
	}

	@Override
	public void onMessage(Message message) {

		String flag = message.getFlag(); // flag of the message

		if (flag.equals("ROBOT"))
			send(message.getSender(), new Message(null, "BASE"));

		else if (flag.equals("ASK"))
			sendEmergencies(message.getSender()); // if a robot ask for emergencies
	}

	@Override
	public void onClock() {
		sendAll(new Message(this, "INIT"));
	}

	/**
	 * send destinations to the Node robot
	 * 
	 * @param robot
	 */
	public void sendEmergencies(Node robot) {

		if (idRobot == -1) {
						
			int size_to_send = sensors.size() / 2;
			ArrayList<Sensor> toSend = new ArrayList<>();
			for (int i = 0; i < size_to_send; i++) {
				toSend.add(sensors.get(0));
				sensors.remove(0);
			}
			send(robot, new Message(toSend, "EMERGENCIES"));
			
			idRobot = robot.getID();
		} else if (idRobot != robot.getID()) {
			send(robot, new Message(sensors, "EMERGENCIES"));
		}
	}

	/**
	 * rebuild the spanning tree
	 * 
	 * @param tp (getTopology())
	 */
	public static void reset(Topology tp) {

		for (Node node : tp.getNodes()) {
			if (node instanceof Sensor) {
				((Sensor) node).parent = null;
			}
		}
		for (Link link : tp.getLinks())
			link.setWidth(1);
	}
	
	public ArrayList<Sensor> getSensors() {
		ArrayList<Sensor> sensors = new ArrayList<>();
		
		for (Node node : getTopology().getNodes())
			if (node instanceof Sensor)
				sensors.add(((Sensor) node));
		return sensors;
	}
}