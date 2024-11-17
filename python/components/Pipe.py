import Component
import ast
from math import pi, sqrt

class Pipe(Component.Component):
  def __init__(self, ID, input_node, output_node, pipe_length, pipe_diameter):
    self.ID = ID
    self.input_node = input_node
    self.output_node = output_node
    self.pipe_length = pipe_length
    self.pipe_diameter = pipe_diameter
  
  def __init__(self, line):
    split = line.split(',')
    self.ID = split[0]
    self.input_node = split[1]
    self.output_node = split[2]
    dic = ast.literal_eval(split[3])
    self.pipe_length = dic['pipe_length']
    self.pipe_diameter = dic['pipe_diameter']
   #pipe_length,0,10,pipe_diameter,0,1    
   
  def pdrop(self, mass_flow_rate, fluid_density, dynamic_viscosity):
    pressure_drop = (128*dynamic_viscosity*self.pipe_length*mass_flow_rate)/(pi*self.pipe_diameter**4*fluid_density)
    return pressure_drop
  
  def mrate(self, pressure_drop, fluid_density, dynamic_viscosity):
    mass_flow_rate = (pi*self.pipe_diameter**4*fluid_density*pressure_drop)/(128*dynamic_viscosity*self.pipe_length)
    return mass_flow_rate