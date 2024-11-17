import networkx as nx
import csv
import datetime
import numpy as np

import sys
from components.component import Component
from components.valve import Valve
from components.checkValve import CheckValve
from components.pipe import Pipe
from components.pressureRegulator import PressureRegulator
from components.pressureReliefValve import PressureReliefValve
from components.tank import Tank
from components.burstDisk import BurstDisk
from components.orificePlate import OrificePlate

debug = True

def make_graph(list_of_objects):
    print("MAKING GRAPH")
    for i in range(len(list_of_objects)):
        print("hi", type(list_of_objects[i]))
    G = nx.MultiDiGraph()
    for i in range(len(list_of_objects)):
        G.add_edge(list_of_objects[i].input_node, list_of_objects[i].output_node, component=list_of_objects[i], mass_flow_rate=0)

    '''for node in G.nodes():
        for predecessor in G.predecessors(node):
            for key, edge_data in G[predecessor].items():
                component = edge_data['component']
                print(type(component))
                if isinstance(component, Tank):
                    G.nodes[predecessor]['pressure'] = component.pressure
                    print(component.ID, "me")'''
    for node in G.nodes():
        for predecessor, _, key, edge_data in G.in_edges(node, data=True, keys=True):
            component = edge_data.get('component')
            if isinstance(component, Tank):
                G.nodes[predecessor]['pressure'] = component.pressure
                print(f"Set pressure of {predecessor} to {component.pressure} from Tank {component.ID}")

        G.nodes[node]['pressure_over_time'] = []

    for node in G.nodes():
        if 'pressure' not in G.nodes[node]:
            G.nodes[node]['pressure'] = 0

    for u, v, key in G.edges(keys=True):
        G[u][v][key]['mass_flow_rate_over_time'] = []

    if debug == True :
        for node in G.nodes():
            print(f"Node {node} has pressure: {G.nodes[node]['pressure']}")
    return G


def update_mass_flow_rate(G, node, fluid_density, dynamic_viscosity):
    for u, v, key, data in G.in_edges(node, keys=True, data=True):
        pressure_drop = G.nodes[u]['pressure'] - G.nodes[v]['pressure']
        data['mass_flow_rate'] = data['component'].mrate(pressure_drop, fluid_density, dynamic_viscosity)

    for u, v, key, data in G.out_edges(node, keys=True, data=True):
        pressure_drop = G.nodes[u]['pressure'] - G.nodes[v]['pressure']
        #print("pressure up", G.nodes[u]['pressure'], "pressure down", G.nodes[v]['pressure'])
        data['mass_flow_rate'] = data['component'].mrate(pressure_drop, fluid_density, dynamic_viscosity)

def get_flow_difference(G,node):
    flow_in = 0
    flow_out = 0
    for u, v, data in G.in_edges(node, data=True):
        flow_in += data['mass_flow_rate']
    for u, v, data in G.out_edges(node, data=True):
        flow_out += data['mass_flow_rate']
    flow_difference = flow_out - flow_in
    return flow_difference


def solve_nodes(G, fluid_density, dynamic_viscosity):
    iterations = 0
    max_iterations = 1000
    epsilon = 0.01
    step_constant = 0.1

    flow_differences_list = []
    node_list = list(G.nodes)

    for node in node_list:
        if not G.degree(node) ==1:
            print("not only 1 edge")
            update_mass_flow_rate(G, node, fluid_density, dynamic_viscosity)
            flow_differences_list.append(get_flow_difference(G, node))
        else:
            print("only one edge")
            update_mass_flow_rate(G, node, fluid_density, dynamic_viscosity)
            flow_differences_list.append(get_flow_difference(G, node))
    #flow_differences_list = np.abs(flow_differences_list)
    while max(flow_differences_list) > epsilon:
        for i in range(len(flow_differences_list)):
            flow_differences_list[i] = flow_differences_list[i] - 0.0000001 * np.random.randint(0, 100)

        index = flow_differences_list.index(max(flow_differences_list.copy(), key=abs))
        print("index ", index)
        print(flow_differences_list)
        current_node = node_list[index]

        if len(list(G.predecessors(current_node))) == 0:
            print("no predecessors, this is a tank")
            current_node = node_list[index-1]
            out_edges = list(G.out_edges(current_node, data=True))
            if out_edges:
                component = out_edges[0][2][ 'component']
                #G.nodes[current_node]['pressure'] = component.pressure
                print("boo:  ", flow_differences_list[index])
                G.nodes[current_node]['pressure'] += step_constant * flow_differences_list[index]

        elif len(list(G.successors(current_node))) == 0:
            G.nodes[current_node]['pressure'] = 0

        else:
            print(step_constant * flow_differences_list[index])
            G.nodes[current_node]['pressure'] -= step_constant * flow_differences_list[index]
        print("pressure",G.nodes[current_node]['pressure'])

        update_mass_flow_rate(G, current_node, fluid_density, dynamic_viscosity)
        flow_differences_list[index] = get_flow_difference(G, current_node)
        print("max flow diff",flow_differences_list[index])

        iterations += 1
        if iterations > max_iterations:
            break


def update_graph(G, time_step_size, time_step, fluid_density, dynamic_viscosity):
    current_time = time_step_size * time_step
    for u, v, key in G.edges(keys=True):
        component = G[u][v][key]['component']
        pressure_drop = G.nodes[u]['pressure'] - G.nodes[v]['pressure']
        pressure_upstream = G.nodes[u]['pressure']

        mass_flow_rate = 0
        for _, _, data in G.out_edges(v, data=True):
            mass_flow_rate += data['mass_flow_rate']

        component.update(current_time, pressure_drop, mass_flow_rate, pressure_upstream)

def graph_to_csv(G, time_step_size, time_step_number):
    num_nodes = len(G.nodes)
    num_edges = len(G.edges)

    #current_time = datetime.datetime.now().strftime('%Y-%m-%d-%H%M%S')
    #csv_filename = f"results-{current_time}.csv"
    csv_filename = "output.txt"

    with open(csv_filename, mode='w', newline='') as file:
        writer = csv.writer(file)

        writer.writerow([time_step_size, time_step_number, num_nodes, num_edges])

        for node, data in G.nodes(data=True):
            pressure_over_time = data.get('pressure_over_time', [])
            row = [node] + pressure_over_time
            writer.writerow(row)

        for u, v, data in G.edges(data=True):
            mass_flow_rate_over_time = data.get('mass_flow_rate_over_time', [])
            component = data.get('component', None)
            component_name = component.ID
            row = [component_name] + mass_flow_rate_over_time
            writer.writerow(row)

def time_independent_solver(G, fluid_density, dynamic_viscosity):
    solve_nodes(G, fluid_density, dynamic_viscosity)
    for node in G.nodes:
        G.nodes[node]['pressure_over_time'].append(G.nodes[node]['pressure'])
    #for u, v in G.edges:
        #G[u][v]['mass_flow_rate_over_time'].append(G[u][v]['mass_flow_rate'])
    for u, v, key in G.edges(keys=True):
        G[u][v][key]['mass_flow_rate_over_time'].append(G[u][v][key]['mass_flow_rate'])

def time_solver(G, time_step_size, time_step_number, fluid_density, dynamic_viscosity):
    for i in range(time_step_number):
        time_independent_solver(G, fluid_density, dynamic_viscosity)
        update_graph(G, time_step_size, i, fluid_density, dynamic_viscosity)
    graph_to_csv(G, time_step_size, time_step_number)

