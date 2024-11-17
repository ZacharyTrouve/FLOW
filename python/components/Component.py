
class Component:
  def __init__(self, ID, input_node, output_node):
    self.ID = ID
    self.input_node = input_node
    self.output_node = output_node

  def pressure_drop(self, Q):
    raise NotImplementedError("No pressure drop function")

  def mass_flow_rate(self, pdrop):
    raise NotImplementedError("No mass flow function")

  def update(self, current_time):
    return None
