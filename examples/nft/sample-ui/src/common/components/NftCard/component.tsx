import { Link } from 'react-router-dom';

import { Center, Flex, Image, Text } from '@mantine/core';

const NftCard = ({
  variants,
  linkTo,
  imageUrl,
  logoUrl,
  attributes
}: {
  variants?: 'lg'[];
  linkTo: string;
  imageUrl?: string;
  logoUrl?: string;
  attributes: {
    name: React.ReactNode;
    value: React.ReactNode;
    centered?: boolean;
  }[];
}) => {
  return (
    <Link to={linkTo}>
      <Flex
        direction={'column'}
        wrap={'nowrap'}
        w={variants?.includes('lg') ? '450px' : '300px'}
        mih="400px"
        sx={{
          boxShadow:
            '0px 1px 2px 0px rgba(16, 24, 40, 0.06), 0px 1px 3px 0px rgba(16, 24, 40, 0.10)',
          borderRadius: '8px'
        }}
        bg={'#262836'}
        p="md"
      >
        <Center w="100%" pos={'relative'} mb={30}>
          {imageUrl && <Image src={imageUrl} />}
          {logoUrl && (
            <Image
              w="100%"
              src={logoUrl}
              pos="absolute"
              width={50}
              bottom={-25}
            />
          )}
        </Center>
        {attributes.map((attr) => (
          <Flex direction={'column'} align={attr.centered ? 'center' : 'start'}>
            <Text>{attr.name}</Text>
            <Text color="white">{attr.value}</Text>
          </Flex>
        ))}
      </Flex>
    </Link>
  );
};

export { NftCard };
