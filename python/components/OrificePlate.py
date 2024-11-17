from .component import Component
import ast
from math import pi, sqrt


class OrificePlate(Component):
  '''def __init__(self, ID, input_node, output_node, orifice_diameter, pipe_diameter, Cv):
    self.ID = ID
    self.input_node = input_node
    self.output_node = output_node
    self.orifice_diameter = orifice_diameter
    self.pipe_diameter = pipe_diameter
    self.Cv = Cv'''

  def __init__(self, line):
    split = line.split(',', 3)
    self.ID = split[0]
    self.input_node = split[1]
    self.output_node = split[2]
    dic = ast.literal_eval(split[3])
    self.orifice_diameter = float(dic['orifice_diameter'])
    self.pipe_diameter = float(dic['pipe_diameter'])
    self.Cv = float(dic['Cv'])
    #orifice_diameter,0,1,pipe_diameter,0,1,Cv,0,10

  def pdrop(self, mass_flow_rate, fluid_density, dynamic_viscosity):
    pressure_drop = (8*mass_flow_rate**2)/(pi**2*fluid_density*self.orifice_diameter**4)
    return pressure_drop
  
  def mrate(self, pressure_drop, fluid_density, dynamic_viscosity):
    mass_flow_rate = sqrt((pi**2*fluid_density*self.orifice_diameter**4*pressure_drop)/(8))
    return mass_flow_rate

  def update(self, current_time, pressure_drop, mass_flow_rate, upstreet_pressure):
      pass