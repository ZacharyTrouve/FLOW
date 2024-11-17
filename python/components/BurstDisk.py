from .component import Component
import ast
from math import pi, sqrt


class BurstDisk(Component):
  '''def __init__(self, ID, input_node, output_node, state, burst_pressure, burst_disk_diameter, Cv):
    self.ID = ID
    self.input_node = input_node
    self.output_node = output_node
    self.state = state
    self.burst_pressure = burst_pressure
    self.burst_disk_diameter = burst_disk_diameter
    self.Cv = Cv'''

  def __init__(self, line):
    split = line.split(',', 3)
    self.ID = split[0]
    self.input_node = split[1]
    self.output_node = split[2]
    dic = ast.literal_eval(split[3])
    self.state = float(dic['state'])
    self.burst_pressure = float(dic['burst_pressure'])
    self.burst_disk_diameter = float(dic['burst_disk_diameter'])
    self.Cv = float(dic['Cv'])
    #state,0,1,burst_pressure,?,?,burst_disk_diameter,0,1,Cv,0,10

  def pdrop(self, mass_flow_rate, fluid_density, dynamic_viscosity):
    pressure_drop = (mass_flow_rate/self.Cv)**2*(fluid_density/2)
    return pressure_drop

  def mrate(self, pressure_drop, fluid_density, dynamic_viscosity):
    mass_flow_rate = self.Cv*sqrt((2*pressure_drop)/(fluid_density))
    return mass_flow_rate
    
  def update(self, current_time, pressure_drop, mass_flow_rate, upstreet_pressure):
    if upstreet_pressure > self.burst_pressure:
      self.state = 1 # flow goes through, never closes