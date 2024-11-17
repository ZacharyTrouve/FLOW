import sys
import components
import Solver

def comp_from_line(line):
    sp = line.split(',', 1)

    COMP = None
    
    if   sp[0] == "valve": COMP = components.Valve(sp[1])
    elif sp[0] == "checkvalve": COMP = components.CheckValve(sp[1])
    elif sp[0] == "pipeNS" or sp[0] == "pipeEW": COMP = components.Pipe(sp[1])
    elif sp[0] == "pressureregulator": COMP = components.PressureRegulator(sp[1])
    elif sp[0] == "pressurereliefvalve": COMP = components.PressureReliefValve(sp[1])
    elif sp[0] == "tank": COMP = components.Tank(sp[1])
    elif sp[0] == "orificeplate": COMP = components.OrificePlate(sp[1])
    elif sp[0] == "burstdisk": COMP = components.BurstDisk(sp[1])

    if (COMP == None): raise ValueError()
    return 



if __name__ == "__main__":
    filename = sys.argv[1]
    comps = []
    with open(filename, 'r') as file:
        for line in file:
            comps.append(comp_from_line(line))

    time_step_size = sys.argv[2]
    time_step_number = sys.argv[3]
    fluid_density = sys.argv[4]
    dynamic_viscosity = sys.argv[5]
    G = Solver.make_graph(comps)
    Solver.time_solver(G, time_step_size, time_step_number, fluid_density, dynamic_viscosity)

