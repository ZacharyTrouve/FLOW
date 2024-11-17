import networkx as nx
import csv
import datetime

from Component import *
from Valve import valve
from python.components.CheckValve import check_valve
from Pipe import pipe
from python.components.PressureRegulator import Regulator
from python.components.PressureReliefValve import pressure_relief_valve
from Tank import tank
from python.components.orificePlate import orifice_plate
from python.components.BurstDisk import burst_disk

def make_graph(list_of_objects):
    G = nx.MultiDiGraph()
    for i in range(len(list_of_objects)):
        G.add_edge(list_of_objects[i].input_node, list_of_objects[i].output_node, component=list_of_objects[i], mass_flow_rate=0)

    for node in G.nodes():
        G.nodes[node]['pressure'] = 0

    for node in G.nodes():
        for predecessor in G.predecessors(node):
            for key, edge_data in G[predecessor].items():
                component = edge_data.get('component', None)
                if isinstance(component, tank):
                    G.nodes[predecessor]['pressure'] = component.pressure

        G.nodes[node]['pressure_over_time'] = []

    for u, v, key in G.edges(keys=True):
        G[u][v][key]['mass_flow_rate_over_time'] = []

    return G


def update_mass_flow_rate(G, node, fluid_density, dynamic_viscosity):
    for u, v, key, data in G.in_edges(node, keys=True, data=True):
        pressure_drop = G.nodes[v]['pressure'] - G.nodes[u]['pressure']
        data['mass_flow_rate'] = data['component'].mrate(pressure_drop, fluid_density, dynamic_viscosity)

    for u, v, key, data in G.out_edges(node, keys=True, data=True):
        pressure_drop = G.nodes[v]['pressure'] - G.nodes[u]['pressure']
        print(data['component'])
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
        update_mass_flow_rate(G, node, fluid_density, dynamic_viscosity)
        flow_differences_list.append(get_flow_difference(G, node))

    while max(flow_differences_list) > epsilon:
        index = flow_differences_list.index(max(flow_differences_list))
        current_node = node_list[index]

        if len(list(G.predecessors(current_node))) == 0:
            G.nodes[current_node]['pressure'] = G.nodes[current_node]['component'].pressure

        elif len(list(G.successors(current_node))) == 0:
            G.nodes[current_node]['pressure'] = 0

        else:
            G.nodes[current_node]['pressure'] += step_constant * flow_differences_list[index]

        update_mass_flow_rate(G, current_node, fluid_density, dynamic_viscosity)
        flow_differences_list[index] = get_flow_difference(G, current_node)

        iterations += 1
        if iterations > max_iterations:
            break


def update_graph(G, time_step_size, time_step):
    current_time = time_step_size * time_step
    for u, v in G.edges:
        component = G[u][v]['component']

        if isinstance(component, Tank):
            mass_flow_rate = 0
            for _, _, data in G.out_edges(v, data=True):
                mass_flow_rate += data['mass_flow_rate']
            component.update(current_time, mass_flow_rate, time_step_size)

        elif isinstance(component, pressure_relief_valve):
            pressure_upstream = G[u]['pressure']
            component.update(pressure_upstream)

        else:
            pressure_drop = G.nodes[v]['pressure'] - G.nodes[u]['pressure']
            G[u][v]['component'].update(current_time, pressure_drop)


def graph_to_csv(G, time_step_size, time_step_number):
    num_nodes = len(G.nodes)
    num_edges = len(G.edges)

    current_time = datetime.datetime.now().strftime('%Y-%m-%d-%H%M%S')
    csv_filename = f"results-{current_time}.csv"

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
    for u, v in G.edges:
        G[u][v]['mass_flow_rate_over_time'].append(G[u][v]['mass_flow_rate'])

def time_solver(G, time_step_size, time_step_number, fluid_density, dynamic_viscosity):
    for i in range(time_step_number):
        time_independent_solver(G, fluid_density, dynamic_viscosity)
        update_graph(G, time_step_size, i)
    graph_to_csv(G, time_step_size, time_step_number)

