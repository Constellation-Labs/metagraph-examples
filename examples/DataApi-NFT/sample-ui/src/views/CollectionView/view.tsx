import { SequenceSpinner } from 'react-spinners-kit';
import { useParams } from 'react-router-dom';
import { useEffect } from 'react';

import ConstellationLogo from '@/assets/logos/constellation_logo.svg';
import ConstellationBanner from '@/assets/illustrations/constellation_banner.png';
import { useFetchableResource } from '@/common/hooks/useFetchableResource.ts';
import { Center, Flex, Grid, Title, useMantineTheme } from '@mantine/core';
import {
  ICollectionAPIResponse,
  ICollectionNFTAPIResponse,
  IMetagraphNftCollection
} from '@/common/types/nfts.ts';
import { NftCard } from '@/common/components/index.ts';
import { shortenAddress } from '@/utils/shorten_address.ts';
import { useApiProvider } from '@/common/providers/index.ts';

const CollectionView = () => {
  const { api } = useApiProvider();
  const { collectionId } = useParams();
  const theme = useMantineTheme();
  const collection = useFetchableResource<IMetagraphNftCollection | null>(null);

  const fetchCollection = collection.wrappedFetch(async () => {
    const collectionResponse = await api.get<ICollectionAPIResponse>(
      `/data-application/collections/${collectionId}`
    );

    const collectionNftsResponse = await api.get<ICollectionNFTAPIResponse[]>(
      `/data-application/collections/${collectionId}/nfts`
    );

    return {
      name: collectionResponse.data.name,
      address: collectionResponse.data.id,
      ownerAddress: collectionResponse.data.owner,
      headerUrl: `https://picsum.photos/seed/${collectionResponse.data.id}/800/400`,
      items: collectionNftsResponse.data.map((item) => ({
        serial: String(item.id).padStart(4, '0'),
        name: item.name,
        collectionAddress: item.collectionId,
        ownerAddress: item.owner,
        illustrationUrl: item.uri,
        traits: []
      }))
    } satisfies IMetagraphNftCollection;
  });

  useEffect(() => {
    fetchCollection();
  }, []);

  return (
    <>
      {collection.resource === null && (
        <Center h="80vh">
          <SequenceSpinner
            size={80}
            frontColor={theme.colors.fireorange[3]}
            backColor={theme.colors.fireorange[3]}
          />
        </Center>
      )}
      {collection.resource !== null && (
        <Flex w="100%" direction={'column'} wrap={'nowrap'} rowGap={'lg'}>
          <Center
            h="60vh"
            w="100%"
            sx={{
              backgroundImage: `url("${ConstellationBanner}")`,
              backgroundSize: 'cover',
              backgroundPositionX: 'center'
            }}
          >
            <NftCard
              linkTo={'.'}
              imageUrl={
                collection.resource.items?.[0]?.illustrationUrl ??
                `https://picsum.photos/seed/${collection.resource.address}/1800/500`
              }
              logoUrl={ConstellationLogo}
              attributes={[
                {
                  name: 'Collection',
                  value: collection.resource.name,
                  centered: true
                },
                {
                  name: 'Owner',
                  value: shortenAddress(collection.resource.ownerAddress),
                  centered: true
                },
                {
                  name: '#NFTs',
                  value: collection.resource.items.length,
                  centered: true
                }
              ]}
            />
          </Center>
          <Center>
            <Title order={2}>NFTs</Title>
          </Center>
          <Grid w="100%" columns={4}>
            {collection.resource.items.map((item) => (
              <Grid.Col span={1}>
                <Center>
                  <NftCard
                    linkTo={`nfts/${item.serial}`}
                    imageUrl={item.illustrationUrl}
                    logoUrl={ConstellationLogo}
                    attributes={[
                      { name: 'Name', value: item.name },
                      {
                        name: 'Owner',
                        value: shortenAddress(item.ownerAddress)
                      }
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

export { CollectionView };
