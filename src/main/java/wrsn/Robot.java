package wrsn;

import java.util.ArrayList;
import java.util.Collections;

import io.jbotsim.core.*;
import io.jbotsim.ui.icons.Icons;

public class Robot extends WaypointNode {

	Node base = null;

	ArrayList<Sensor> emergencies = new ArrayList<>();// choose the closest neighbor in this list
	ArrayList<Sensor> sensors = new ArrayList<>();// a half of all sensors

	@Override
	public void onStart() {

		setSensingRange(30);
		setIcon(Icons.ROBOT);
		setIconSize(16);

		sendAll(new Message(null, "ROBOT"));
	}

	@Override
	public void onSensingIn(Node node) {
		if (node instanceof Sensor) {
			Sensor sensor = (Sensor) node;

			if (!sensor.isAlive()) {
				sensor.battery = 255;
				BaseStation.reset(getTopology());
				return;
			}
			sensor.battery = 255;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onMessage(Message message) {
		String flag = message.getFlag();
		if (flag.equals("BASE")) {
			base = message.getSender(); // Initiate the BaseStation and go there
			addDestination(base.getX(), base.getY());
		}
		if (flag.equals("EMERGENCIES")) {
			sensors.addAll((ArrayList<Sensor>) message.getContent()); // add all destinations
		}

	}

	@Override
	public void onArrival() {
		if (getLocation().equals(base.getLocation())) {
			send(base, new Message(null, "ASK")); // ASK for emergencies
		}
		
		if (sensors.isEmpty()) {
			destinations.add(base.getLocation());
			return;
		}
		
		if (emergencies.isEmpty()) {
			
			Collections.sort(sensors);
			emergencies.addAll(sensors.subList(0, 3)); // 
			
			choose_destination();
		} else {
			choose_destination(); // choose the closest neighbor
		}
	}

	/**
	 * go to the closest neighbor
	 */
	public void choose_destination() {

		int index_min_distance = 0;
		double min_distance = distA_To_B(getLocation(), emergencies.get(0).getLocation());

		for (int i = 1; i < emergencies.size(); i++) {
			Sensor s_i = emergencies.get(i);
			double newdist = distA_To_B(getLocation(), s_i.getLocation());// distance to Sensor #i

			if (newdist < min_distance) {
				min_distance = newdist;
				index_min_distance = i;
			}
		}
		destinations.add(emergencies.get(index_min_distance).getLocation());
		emergencies.remove(index_min_distance);
	}

	/**
	 * 
	 * @param a Point
	 * @param b Point
	 * @return Euclidean distance between a b
	 */
	public double distA_To_B(Point a, Point b) {
		return Math.sqrt((Math.pow(b.getX() - a.getX(), 2)) + (Math.pow(b.getY() - a.getY(), 2)));
	}

}