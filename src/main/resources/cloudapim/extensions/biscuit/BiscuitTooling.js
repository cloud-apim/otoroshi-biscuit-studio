class DatalogPlayground extends React.Component {
  constructor(props) {
		super(props);
  }

  componentDidMount() {
    this.props.setTitle(`Biscuit Datalog playground`);
  }
  render() {
    return (
      React.createElement("div", { className: 'biscuit-datalog-playground' },
        React.createElement("bc-datalog-playground", { showBlocks: true },
          React.createElement("code",{ className: "block" },"operation(\"read\");\n resource(\"file1\");"),
          React.createElement("code",{ className: "block", privateKey: 'ffca92f0d85740520ac27343ee563f0821e6c6fbc7a565e406687c894d67c912' },"group(\"admin\");"),
          React.createElement("code",{ className: "authorizer" },"check if group(\"admin\") trusting ed25519/ca34b6fcff1f68398d0ffca59086e6c12b3408c8e11454d1523591acd24b5562;\n      allow if true;"),
        )
      )
    )
  }
}

class TokenInspector extends React.Component {
  constructor(props) {
    super(props);
  }

  componentDidMount() {
    this.props.setTitle(`Biscuit token inspector`);
  }
  render() {
    return (
      React.createElement("div", { className: 'biscuit-token-inspector' },
        React.createElement("bc-token-printer", { showAuthorizer: true })
      )
    )
  }
}

class TokenAttenuator extends React.Component {
  constructor(props) {
    super(props);
  }

  componentDidMount() {
    this.props.setTitle(`Biscuit token attenuator`);
  }
  render() {
    return (
      React.createElement("div", { className: 'biscuit-token-attenuator' },
        React.createElement("bc-token-printer", { showAttenuation: true })
      )
    )
  }
}