const getAllProperties = (object: any) => {
  const properties = new Set<[any, string | number | symbol]>();

  do {
    for (const key of Reflect.ownKeys(object)) {
      properties.add([object, key]);
    }
  } while (
    (object = Reflect.getPrototypeOf(object)) &&
    object !== Object.prototype
  );

  return properties;
};

const bindAllMethods = (self: any) => {
  for (const [object, key] of getAllProperties(self.constructor.prototype)) {
    if (key === 'constructor') {
      continue;
    }

    const descriptor = Reflect.getOwnPropertyDescriptor(object, key);
    if (descriptor && typeof descriptor.value === 'function') {
      self[key] = self[key].bind(self);
    }
  }

  return self;
};

export { bindAllMethods };
