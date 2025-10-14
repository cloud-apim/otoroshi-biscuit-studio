import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';

import Heading from '@theme/Heading';
import styles from './index.module.css';

function HomepageHeader() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <header className={clsx('hero hero--primary', styles.heroBanner)}>
      <div className="container">
        <Heading as="h1" className="hero__title">
          {siteConfig.title}
        </Heading>
        <p className="hero__subtitle">{siteConfig.tagline}</p>
        <img src={require('@site/static/img/otoroshi-biscuit-studio-logo-no-bg-no-text.png').default} alt="Otoroshi Biscuit Studio Logo" style={{ width: 250 }} />
        <div className={styles.buttons}>
          <a
            className="button button--secondary button--lg"
            href="https://github.com/cloud-apim/otoroshi-biscuit-studio/releases/latest" target="_blank">
            Download latest version
          </a>
        </div>
      </div>
    </header>
  );
}

export default function Home() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout
      title={`Hello from ${siteConfig.title}`}
      description="Description will go into a meta tag in <head />">
      <HomepageHeader />
      <main>
        <HomepageFeatures />
      </main>
      <div style={{ width: '100%', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
        <h3>Funding</h3>
        <p>
          This project was funded by the French Government under the <a href="https://www.economie.gouv.fr/france-2030">France 2030 plan</a>, operated by <a href="https://www.capdigital.com/">Cap Digital</a> and <a href="https://www.bpifrance.fr/">Bpifrance</a>, and is supported by the European Union – <a href="https://next-generation-eu.europa.eu/">NextGenerationEU</a>.
        </p>
        <div>
          <img src="https://www.info.gouv.fr/upload/media/organization/0001/01/sites_default_files_contenu_illustration_2022_03_logotype-blanc.jpg" width="100" height="100" />
          <img src="https://presse.bpifrance.fr/wp-content/uploads/2025/06/13dd09b1598e84ec026a9181fe6988a3-l.jpg" width="200" height="100" />
          <img src="https://hopital-europeen.fr/sites/default/files/wysiwyg/etiquette_France_Relance_UE.png" width="200" height="100" />
        </div>
      </div>
    </Layout>
  );
}
