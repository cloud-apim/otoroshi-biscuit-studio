import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import Heading from '@theme/Heading';
import styles from './index.module.css';

const stats = [
  { number: '6', label: 'Entity Types' },
  { number: '7', label: 'Route Plugins' },
  { number: 'RBAC', label: 'Policy Engine' },
  { number: 'OAuth2', label: 'Client Credentials' },
];

const features = [
  {
    icon: '\uD83D\uDD10',
    title: 'KeyPair Management',
    description: 'Create and manage cryptographic keypairs for signing, attenuating and verifying Biscuit tokens. The foundation of your token security infrastructure.',
  },
  {
    icon: '\uD83D\uDD12',
    title: 'Token Verification',
    description: 'Validate incoming Biscuit tokens against defined rules and policies. Ensure only properly authorized requests reach your services.',
  },
  {
    icon: '\uD83C\uDFDB\uFE0F',
    title: 'Token Forging',
    description: 'Generate tokens from configurable templates with facts, rules and checks. Define once, forge consistently across your entire infrastructure.',
  },
  {
    icon: '\uD83C\uDF9B\uFE0F',
    title: 'Token Attenuation',
    description: 'Reduce token capabilities to grant only minimal required permissions. Fine-grained scope control for every API route.',
  },
  {
    icon: '\uD83D\uDC65',
    title: 'RBAC Policies',
    description: 'Implement Role-Based Access Control using Biscuit tokens. Structured, flexible access mechanisms for secure role-based user management.',
  },
  {
    icon: '\uD83C\uDF10',
    title: 'Remote Facts Loader',
    description: 'Integrate external data sources to enhance authorization decisions. Dynamic, context-aware access control powered by real-time data.',
  },
  {
    icon: '\uD83D\uDD11',
    title: 'Client Credentials Flow',
    description: 'OAuth2 client_credentials flow with Biscuit tokens as access tokens. Standards-compliant authentication for machine-to-machine communication.',
  },
  {
    icon: '\uD83D\uDC64',
    title: 'User Extraction',
    description: 'Extract user identity from Biscuit tokens and forward it to backend services. Seamless user identification without additional auth mechanisms.',
  },
  {
    icon: '\uD83D\uDCE1',
    title: 'Public Key Exposition',
    description: 'Expose your public keys through .well-known/biscuit-web-keys endpoints. Enable third-party token verification with standard discovery.',
  },
];

const whyCards = [
  {
    icon: '\uD83C\uDFD7\uFE0F',
    title: 'Built on Otoroshi',
    description: 'Leverage a battle-tested, cloud-native API gateway. Get mTLS, service mesh, plugins, and admin UI out of the box. Your token security inherits enterprise-grade infrastructure.',
  },
  {
    icon: '\uD83D\uDEE1\uFE0F',
    title: 'Biscuit Cryptographic Security',
    description: 'Powered by Eclipse Biscuit tokens \u2014 cutting-edge cryptographic authorization combining public-key signatures with a logic-based policy language. Offline verification, no central authority needed.',
  },
  {
    icon: '\uD83D\uDD17',
    title: 'Hierarchical Delegation',
    description: 'Delegate permissions across organizational boundaries while maintaining strict policy control. Each delegation layer can only restrict, never expand \u2014 security by design.',
  },
  {
    icon: '\uD83C\uDF0D',
    title: 'Sovereign & Open Source',
    description: 'Run on your infrastructure, keep your data where it belongs. Fully open source under Apache 2.0. Funded by the French Government under the France 2030 plan.',
  },
];

const plugins = [
  'Biscuit Verifier',
  'Biscuit Attenuator',
  'Client Credentials',
  'User Extractor',
  'User to Biscuit',
  'ApiKey Bridge',
  'Public Keys Exposition',
];

const useCases = [
  {
    icon: '\uD83C\uDFE2',
    title: 'API Security Gateway',
    description: 'Centralize token-based access control across all your API routes with cryptographic proof.',
  },
  {
    icon: '\uD83D\uDD00',
    title: 'Fine-Grained Authorization',
    description: 'Go beyond simple API keys with policy-based, attenuated permissions for every endpoint.',
  },
  {
    icon: '\uD83D\uDEE1\uFE0F',
    title: 'Zero Trust Architecture',
    description: 'Verify every request with offline cryptographic proof and auditable, embedded policies.',
  },
  {
    icon: '\u26A1',
    title: 'Microservices Security',
    description: 'Delegate and attenuate permissions across service boundaries with compact, efficient tokens.',
  },
];

function HomepageHeader() {
  return (
    <header className={styles.heroBanner}>
      <div className="container">
        <div className={styles.heroLayout}>
          <div className={styles.heroContent}>
            <div className={styles.heroTagline}>Next-Gen Token Security for APIs</div>
            <Heading as="h1" className={styles.heroTitle}>
              The <span className={styles.heroTitleAccent}>Token Security</span> Your APIs Deserve
            </Heading>
            <p className={styles.heroSubtitle}>
              Integrate Eclipse Biscuit tokens into your API gateway for cryptographic,
              fine-grained access control. Forge, verify, attenuate, and delegate
              permissions &mdash; powered by Otoroshi.
            </p>
            <div className={styles.heroButtons}>
              <Link className={styles.heroPrimary} to="/docs/overview">
                Get Started
              </Link>
              <Link
                className={styles.heroSecondary}
                href="https://github.com/cloud-apim/otoroshi-biscuit-studio/releases/latest">
                Download
              </Link>
              <Link
                className={styles.heroGithub}
                href="https://github.com/cloud-apim/otoroshi-biscuit-studio">
                GitHub
              </Link>
            </div>
          </div>
          <div className={styles.heroMascotWrapper}>
            <img
              src={require('@site/static/img/otoroshi-biscuit-studio-logo-no-bg-no-text.png').default}
              alt="Otoroshi Biscuit Studio"
              className={styles.heroMascot}
            />
          </div>
        </div>
      </div>
    </header>
  );
}

function StatsStrip() {
  return (
    <section className={styles.statsStrip}>
      <div className="container">
        <div className={styles.statsGrid}>
          {stats.map((stat, idx) => (
            <div key={idx} className={styles.statItem}>
              <span className={styles.statNumber}>{stat.number}</span>
              <span className={styles.statLabel}>{stat.label}</span>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function FeaturesSection() {
  return (
    <section className={styles.featuresSection}>
      <div className="container">
        <div className={styles.sectionHeader}>
          <div className={styles.sectionTag}>Capabilities</div>
          <Heading as="h2" className={styles.sectionTitle}>
            Everything You Need for Token-Based Security
          </Heading>
          <p className={styles.sectionSubtitle}>
            From key management to token forging, from verification to attenuation &mdash;
            a complete toolkit for production-grade Biscuit token infrastructure.
          </p>
        </div>
        <div className={styles.featuresGrid}>
          {features.map((feature, idx) => (
            <div key={idx} className={styles.featureCard}>
              <span className={styles.featureIcon}>{feature.icon}</span>
              <div className={styles.featureTitle}>{feature.title}</div>
              <p className={styles.featureDesc}>{feature.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function WhySection() {
  return (
    <section className={styles.whySection}>
      <div className="container">
        <div className={styles.sectionHeader}>
          <div className={styles.sectionTag}>Why Choose Us</div>
          <Heading as="h2" className={styles.sectionTitle}>
            Why Otoroshi Biscuit Studio?
          </Heading>
          <p className={styles.sectionSubtitle}>
            Not just another auth layer. A complete Biscuit token management
            platform built for teams that take API security seriously.
          </p>
        </div>
        <div className={styles.whyGrid}>
          {whyCards.map((card, idx) => (
            <div key={idx} className={styles.whyCard}>
              <span className={styles.whyIcon}>{card.icon}</span>
              <div className={styles.whyTitle}>{card.title}</div>
              <p className={styles.whyDesc}>{card.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function PluginsSection() {
  return (
    <section className={styles.pluginsSection}>
      <div className="container">
        <div className={styles.sectionHeader}>
          <div className={styles.sectionTag}>Plugins</div>
          <Heading as="h2" className={styles.sectionTitle}>
            7 Otoroshi Plugins, Ready to Deploy
          </Heading>
          <p className={styles.sectionSubtitle}>
            Drop-in plugins for your Otoroshi routes. Add Biscuit token security
            to any API endpoint in minutes, no code changes required.
          </p>
        </div>
        <div className={styles.pluginsCloud}>
          {plugins.map((plugin, idx) => (
            <span key={idx} className={styles.pluginBadge}>{plugin}</span>
          ))}
          <Link
            className={`${styles.pluginBadge} ${styles.pluginMore}`}
            to="/docs/overview">
            Explore all
          </Link>
        </div>
      </div>
    </section>
  );
}

function UseCasesSection() {
  return (
    <section className={styles.useCasesSection}>
      <div className="container">
        <div className={styles.sectionHeader}>
          <div className={styles.sectionTag}>Use Cases</div>
          <Heading as="h2" className={styles.sectionTitle}>
            Built for Real-World Security Scenarios
          </Heading>
          <p className={styles.sectionSubtitle}>
            From startups to enterprises, secure your APIs with confidence.
          </p>
        </div>
        <div className={styles.useCasesGrid}>
          {useCases.map((uc, idx) => (
            <div key={idx} className={styles.useCaseCard}>
              <span className={styles.useCaseIcon}>{uc.icon}</span>
              <div className={styles.useCaseTitle}>{uc.title}</div>
              <p className={styles.useCaseDesc}>{uc.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function CtaSection() {
  return (
    <section className={styles.ctaSection}>
      <div className="container">
        <div className={styles.ctaBox}>
          <div className={styles.ctaContent}>
            <Heading as="h2" className={styles.ctaTitle}>
              Ready to Secure Your APIs with Biscuit Tokens?
            </Heading>
            <p className={styles.ctaSubtitle}>
              Get started in minutes. Open source, free forever.
            </p>
            <div className={styles.ctaButtons}>
              <Link className={styles.heroPrimary} to="/docs/overview">
                Read the Docs
              </Link>
              <Link
                className={styles.heroSecondary}
                href="https://discord.cloud-apim.com">
                Join the Community
              </Link>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

function FundingSection() {
  return (
    <section className={styles.fundingSection}>
      <div className="container">
        <div className={styles.fundingTitle}>Funding</div>
        <p className={styles.fundingText}>
          This project was funded by the French Government under the{' '}
          <a href="https://www.economie.gouv.fr/france-2030">France 2030 plan</a>,
          operated by <a href="https://www.capdigital.com/">Cap Digital</a> and{' '}
          <a href="https://www.bpifrance.fr/">Bpifrance</a>, and is supported by the
          European Union &ndash; <a href="https://next-generation-eu.europa.eu/">NextGenerationEU</a>.
        </p>
        <div className={styles.fundingLogos}>
          <img src="https://www.info.gouv.fr/upload/media/organization/0001/01/sites_default_files_contenu_illustration_2022_03_logotype-blanc.jpg" width="100" height="100" alt="French Government" />
          <img src="https://presse.bpifrance.fr/wp-content/uploads/2025/06/13dd09b1598e84ec026a9181fe6988a3-l.jpg" width="200" height="100" alt="Bpifrance" />
          <img src="https://hopital-europeen.fr/sites/default/files/wysiwyg/etiquette_France_Relance_UE.png" width="200" height="100" alt="NextGenerationEU" />
        </div>
      </div>
    </section>
  );
}

export default function Home() {
  const { siteConfig } = useDocusaurusContext();
  return (
    <Layout
      title="The Token Security Your APIs Deserve"
      description="Integrate Eclipse Biscuit tokens into your API gateway for cryptographic, fine-grained access control. Forge, verify, attenuate, and delegate permissions — powered by Otoroshi.">
      <HomepageHeader />
      <main>
        <StatsStrip />
        <FeaturesSection />
        <WhySection />
        <PluginsSection />
        <UseCasesSection />
        <CtaSection />
        <FundingSection />
      </main>
    </Layout>
  );
}
