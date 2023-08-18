import React from 'react';

type ProviderPair<P> = [
  React.ComponentType<P>,
  Omit<React.ComponentProps<React.ComponentType<P>>, 'children'>
];

const ProviderStack = ({
  providers,
  children
}: {
  providers: ProviderPair<any>[];
  children: React.ReactNode;
}) => {
  let currentChild = children;

  for (const [Provider, props] of [...providers].reverse()) {
    currentChild = <Provider {...props}>{currentChild}</Provider>;
  }

  return <>{currentChild}</>;
};

const createProviderStack = () => {
  const providers: ProviderPair<any>[] = [];

  const addProvider = <T extends any>(
    component: ProviderPair<T>['0'],
    props: ProviderPair<T>['1']
  ) => {
    providers.push([component, props]);
  };

  const wrapComponent = <P extends any>(
    component: React.ComponentType<P>
  ): React.ComponentType<P> => {
    const Component = component;
    return (props) => (
      <ProviderStackBundled>
        {<Component {...(props as any)} />}
      </ProviderStackBundled>
    );
  };

  const ProviderStackBundled = ({
    children
  }: {
    children: React.ReactNode;
  }) => <ProviderStack providers={providers}>{children}</ProviderStack>;

  return Object.assign(ProviderStackBundled, { addProvider, wrapComponent });
};

export { ProviderStack, createProviderStack };
