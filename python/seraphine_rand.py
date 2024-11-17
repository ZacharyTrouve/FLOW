from Component import *
from Valve import Valve
from CheckValve import CheckValve
from Pipe import Pipe
from PressureRegulator import Regulator
from PressureReliefValve import PressureReliefValve
from Tank import Tank
from OrificePlate import OrificePlate
from BurstDisk import BurstDisk
from Solver import *

import networkx as nx
import csv
import datetime


test_tank = Tank(ID=1, input_node=0, output_node=1, mass=100, pressure=850)
test_orifice_plate = OrificePlate(ID=2, input_node=1, output_node=2, orifice_diameter=0.02, pipe_diameter=0.2, Cv=0.315)
test_pipe = Pipe(ID=3, input_node=2, output_node=3, pipe_length=1, pipe_diameter=0.1)
test_pressure_relief_valve = PressureReliefValve(ID=4, input_node=3, output_node=4, valve_diameter=0.02, burst_pressure=800, Cv=0.315, initial_state = 0)
## optional
test_burst_disk = BurstDisk(ID=5, input_node=3, output_node=4, state=0, burst_pressure=825, burst_disk_diameter=0.05, Cv=0.315)

objects_list = [test_tank, test_orifice_plate, test_pipe, test_pressure_relief_valve, test_burst_disk]

time_step_size = 1
time_step_number = 10
fluid_density = 1
dynamic_viscosity = 1
G = make_graph(objects_list)
time_solver(G, time_step_size, time_step_number, fluid_density, dynamic_viscosity)