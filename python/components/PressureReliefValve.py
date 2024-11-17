from .component import Component
import ast
from math import pi, sqrt
  

class PressureReliefValve(Component):
  '''def __init__(self, ID, input_node, output_node, initial_state, valve_diameter, burst_pressure, Cv):
    self.ID = ID
    self.input_node = input_node
    self.output_node = output_node
    self.initial_state = initial_state
    self.state = initial_state
    self.valve_diameter = valve_diameter
    self.burst_pressure = burst_pressure
    self.Cv = Cv'''

  def __init__(self, line):
    split = line.split(',', 3)
    self.ID = split[0]
    self.input_node = split[1]
    self.output_node = split[2]
    dic = ast.literal_eval(split[3])
    self.initial_state = float(dic['initial_state'])
    self.state = float(dic['state'])
    self.valve_diameter = float(dic['valve_diameter'])
    self.burst_pressure = float(dic['burst_pressure'])
    self.Cv = float(dic['Cv'])
    #initial_state,0,1,state,0,1,valve_diameter,0,1,burst_pressure,0,10000000


  ## pressure drop
  def pdrop(self, mass_flow_rate, fluid_density, dynamic_viscosity): ## pressure drop in terms of Cv, fluid density, mass flow rate
    pressure_drop = (mass_flow_rate/self.Cv)**2*(fluid_density/2)
    return pressure_drop
  ## mass flow rate
  def mrate(self, pressure_drop, fluid_density, dynamic_viscosity):
    mass_flow_rate = self.Cv*sqrt((2*pressure_drop)/(fluid_density))
    return mass_flow_rate

  def update(self, current_time, pressure_drop, mass_flow_rate, upstreet_pressure):
    ## this valve only opens when the upstreet pressure exceeds burst pressure
    if upstreet_pressure >= self.burst_pressure:
      self.state = 1 # valve open, flow goes through
    else:
      self.state = 0 # no flow
