import Component
import ast
from math import pi, sqrt


class Tank(Component.Component):
  def __init__(self, ID, input_node, output_node, mass, pressure):
    self.ID = ID
    self.input_node = input_node
    self.output_node = output_node
    self.initial_mass = mass
    self.mass = mass
    self.pressure = pressure
    self.time = 0

  def __init__(self, line):
    split = line.split(',', 3)
    self.ID = split[0]
    self.input_node = split[1]
    self.output_node = split[2]
    dic = ast.literal_eval(split[3])
    self.initial_mass = dic['initial_mass']
    self.mass = dic['mass']
    self.pressure = dic['pressure']
    self.time = dic['time']
    #initial_mass,0,10000000,mass,0,10000000,pressure,0,10000000,time,0,inf

      
  ## tank has a pressure drop depending on mass flow rate
  def pdrop(self, mass_flow_rate, fluid_density, dynamic_viscosity):
    pressure_drop = (mass_flow_rate/self.Cv)**2*(fluid_density/2)
    return pressure_drop
  
  def mrate(self, pressure_drop, fluid_density, dynamic_viscosity):
    mass_flow_rate = self.Cv*sqrt((2*pressure_drop)/(fluid_density))
    return mass_flow_rate
  
  def update(self, current_time, pressure_drop, mass_flow_rate, upstreet_pressure):
    ## update mass: mass = current mass - mass flow * time step
    self.mass = self.mass - mass_flow_rate * (current_time - self.time)
    ## update pressure
    if self.mass == 0: ## when mass is depleted, pressure set to 0
      self.pressure = 0
    ## update current time
    self.time = current_time