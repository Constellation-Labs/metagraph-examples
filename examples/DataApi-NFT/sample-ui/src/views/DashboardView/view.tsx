import { SequenceSpinner } from 'react-spinners-kit';
import { useEffect } from 'react';

import ConstellationLogo from '@/assets/logos/constellation_logo.svg';
import { useFetchableResource } from '@/common/hooks/useFetchableResource.ts';
import { Center, Flex, Grid, Title, useMantineTheme } from '@mantine/core';
import {
  ICollectionAPIResponse,
  IMetagraphNftCollection
} from '@/common/types/nfts.ts';
import { NftCard } from '@/common/components/index.ts';
import { shortenAddress } from '@/utils/shorten_address.ts';
import { useApiProvider } from '@/common/providers/index.ts';

const DashboardView = () => {
  const { api } = useApiProvider();
  const theme = useMantineTheme();
  const collections = useFetchableResource<IMetagraphNftCollection[] | null>(
    null
  );

  const fetchCollections = collections.wrappedFetch(async () => {
    const response = await api.get<ICollectionAPIResponse[]>(
      '/data-application/collections'
    );

    return response.data.map(
      (item) =>
        ({
          name: item.name,
          address: item.id,
          ownerAddress: item.owner,
          headerUrl: `https://picsum.photos/seed/${item.id}/1800/500`,
          length: item.numberOfNFTs,
          items: []
        } satisfies IMetagraphNftCollection)
    );
  });

  useEffect(() => {
    fetchCollections();
  }, []);

  return (
    <>
      {collections.resource === null && (
        <Center h="80vh">
          <SequenceSpinner
            size={80}
            frontColor={theme.colors.fireorange[3]}
            backColor={theme.colors.fireorange[3]}
          />
        </Center>
      )}
      {collections.resource !== null && (
        <Flex
          w="100%"
          direction={'column'}
          wrap={'nowrap'}
          rowGap={'lg'}
          pt="lg"
        >
          <Center>
            <Title order={2}>Collections</Title>
          </Center>
          <Grid w="100%" columns={4}>
            {collections.resource.map((item) => (
              <Grid.Col span={1}>
                <Center>
                  <NftCard
                    linkTo={`/collections/${item.address}`}
                    logoUrl={ConstellationLogo}
                    attributes={[
                      { name: 'Collection Name', value: item.name },
                      {
                        name: 'Owner',
                        value: shortenAddress(item.ownerAddress)
                      },
                      { name: '#NFTs', value: item.length ?? 0 }
                    ]}
                  />
                </Center>
              </Grid.Col>
            ))}
          </Grid>
        </Flex>
      )}
    </>
  );
};

export { DashboardView };
