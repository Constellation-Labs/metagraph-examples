import { SequenceSpinner } from 'react-spinners-kit';
import { useParams } from 'react-router-dom';
import { useEffect } from 'react';

import ConstellationLogo from '@/assets/logos/constellation_logo.svg';
import { useFetchableResource } from '@/common/hooks/useFetchableResource.ts';
import {
  Center,
  Flex,
  Space,
  Text,
  Title,
  useMantineTheme
} from '@mantine/core';
import {
  ICollectionNFTAPIResponse,
  IMetagraphNft
} from '@/common/types/nfts.ts';
import { NftCard } from '@/common/components/index.ts';
import { shortenAddress } from '@/utils/shorten_address.ts';
import { useApiProvider } from '@/common/providers/index.ts';

const NftView = () => {
  const { api } = useApiProvider();
  const { collectionId, nftId } = useParams();
  const theme = useMantineTheme();
  const nft = useFetchableResource<IMetagraphNft | null>(null);

  const fetchCollection = nft.wrappedFetch(async () => {
    const nftResponse = await api.get<ICollectionNFTAPIResponse>(
      `/data-application/collections/${collectionId}/nfts/${parseInt(
        nftId ?? '0'
      )}`
    );

    return {
      serial: String(nftResponse.data.id).padStart(4, '0'),
      name: nftResponse.data.name,
      collectionAddress: nftResponse.data.collectionId,
      ownerAddress: nftResponse.data.owner,
      illustrationUrl: nftResponse.data.uri,
      traits: []
    } satisfies IMetagraphNft;
  });

  useEffect(() => {
    fetchCollection();
  }, []);

  return (
    <>
      {nft.resource === null && (
        <Center h="80vh">
          <SequenceSpinner
            size={80}
            frontColor={theme.colors.fireorange[3]}
            backColor={theme.colors.fireorange[3]}
          />
        </Center>
      )}
      {nft.resource !== null && (
        <Center w="100%">
          <Flex w={1200} p={'lg'} columnGap={'lg'}>
            <NftCard
              variants={['lg']}
              linkTo={'.'}
              imageUrl={nft.resource.illustrationUrl}
              logoUrl={ConstellationLogo}
              attributes={[
                { name: 'Name', value: nft.resource.name },
                {
                  name: 'Owner',
                  value: shortenAddress(nft.resource.ownerAddress)
                }
              ]}
            />
            <Flex direction={'column'} rowGap={'xs'}>
              <Title order={1}>{nft.resource.name}</Title>
              <Text>By ConstellationNetwork Inc.</Text>
              <Space h={100} />
              <Title order={2}>Owner</Title>
              <Text>{nft.resource.ownerAddress}</Text>
              <Space h={100} />
            </Flex>
          </Flex>
        </Center>
      )}
    </>
  );
};

export { NftView };
