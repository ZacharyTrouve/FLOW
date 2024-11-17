import sys
from components.valve import Valve
from components.checkValve import CheckValve
from components.pipe import Pipe
from components.pressureRegulator import PressureRegulator
from components.pressureReliefValve import PressureReliefValve
from components.tank import Tank
from components.burstDisk import BurstDisk
from components.orificePlate import OrificePlate

import Solver

def comp_from_line(line):
    sp = line.split(',', 1)

    COMP = None
    if   sp[0] == "gatevalve": COMP = Valve(sp[1])
    elif sp[0] == "checkvalve": COMP = CheckValve(sp[1])
    elif sp[0] == "pipeNS" or sp[0] == "pipeEW": COMP = Pipe(sp[1])
    elif sp[0] == "pressureregulator": COMP = PressureRegulator(sp[1])
    elif sp[0] == "pressurereliefvalve": COMP = PressureReliefValve(sp[1])
    elif sp[0] == "tank": COMP = Tank(sp[1])
    elif sp[0] == "orificeplate": COMP = OrificePlate(sp[1])
    elif sp[0] == "burstcap": COMP = BurstDisk(sp[1])
    print(sp)
    if (COMP == None): raise ValueError()
    return COMP



if __name__ == "__main__":
    #filename = sys.argv[1]
    filename = "/Users/zachary/Documents/FLOW/data/inputs/input202411161859.txt"
    comps = []
    with open(filename, 'r') as file:
        for line in file:
            sp = line.split(',', 1)
            if sp[0] == "pit":
                pit_id = int(sp[1])
                continue
            comps.append(comp_from_line(line))
    for item in comps: print (type(item))
    '''time_step_size = sys.argv[2]
    time_step_number = sys.argv[3]
    fluid_density = sys.argv[4]
    dynamic_viscosity = sys.argv[5]'''
    time_step_size = 1
    time_step_number = 5
    fluid_density = 1
    dynamic_viscosity = 1
    G = Solver.make_graph(comps)
    Solver.time_solver(G, time_step_size, time_step_number, fluid_density, dynamic_viscosity, pit_id)

