import { Card, PageFrame } from '../components';

export default async function HomePage() {
  return (
    <PageFrame>
      <Card>Sample Content</Card>
      <Card variants={['padding-m']}>Sample Content</Card>
      <Card variants={['padding-sm']}>Sample Content</Card>
      <Card header={'Sample header'}>Sample Content</Card>
      <Card header={'Sample header'} variants={['padding-m']}>
        Sample Content
      </Card>
      <Card header={'Sample header'} variants={['padding-sm']}>
        Sample Content
      </Card>
    </PageFrame>
  );
}
