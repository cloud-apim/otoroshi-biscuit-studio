import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

const FeatureList = [
  {
    title: 'Biscuit Verifiers',
    Svg: require('@site/static/img/undraw_secure-server.svg').default,
    description: (
      <>
        Biscuit Verifiers allow you to validate tokens securely and flexibly. 
        They ensure that permissions and policies are respected at all times.
      </>
    ),
  },
  {
    title: 'Attenuators',
    Svg: require('@site/static/img/undraw_control-panel.svg').default,
    description: (
      <>
        Attenuators enable you to refine or reduce the capabilities of tokens, 
        granting only the minimal required permissions for secure operations.
      </>
    ),
  },
  {
    title: 'Remote Facts Loader',
    Svg: require('@site/static/img/undraw_cloud-sync.svg').default,
    description: (
      <>
        The Remote Facts Loader allows integration of external data seamlessly 
        into token verifications, expanding the scope and dynamism of your system.
      </>
    ),
  },
  {
    title: 'Hierarchical Delegation',
    Svg: require('@site/static/img/undraw_team-collaboration.svg').default,
    description: (
      <>
        Biscuit tokens support hierarchical delegation, enabling flexible delegation 
        of permissions while maintaining strict control over policies.
      </>
    ),
  },
  {
    title: 'Compact Design',
    Svg: require('@site/static/img/undraw_file-analysis.svg').default,
    description: (
      <>
        Biscuit tokens are compact and designed for efficiency, making them ideal for 
        resource-constrained environments and fast transmissions.
      </>
    ),
  },
  {
    title: 'Auditable Policies',
    Svg: require('@site/static/img/undraw_security-on.svg').default,
    description: (
      <>
        Policies embedded in Biscuit tokens are easily auditable, ensuring that 
        security and compliance requirements are met with transparency.
      </>
    ),
  },
];

function Feature({Svg, title, description}) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures() {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}