class GraphicalTokensAttenuator extends Component {
  constructor(props) {
    super(props);
    this.state = {
      token: ""
    };
  }

  useExample = () => {
    this.setState({
      token:
        "EncKDRgDIgkKBwgKEgMQ0gkSJAgAEiDPkePanf8chDmSNa5N19PMGMepAT_eUXvNJR5NfNX7JhpA4apyFF_za3liqw7DRNI0GrbiL9GPihRTQSZh7q06j7_2k0qgC5N6k76YdOjT9DWSA4tABLfLJTmo2FsEPR9vDCIiCiAdLBMHlQCsJbLVJHViqsa9a8D0GCZob8W6VBuEsptU1g==",
    });
  };

  clearAll = () => {
    this.setState({ token: "" });
  };

  render() {
    return React.createElement(
      "div",
      { className: "container py-5" },
      React.createElement("h1", { className: "mb-4 text-white fw-bold text-center" }, "Graphical Tokens Attenuator"),
      React.createElement(
        "div",
        { className: "d-flex flex-wrap justify-content-center gap-3 mb-4" },
        React.createElement(
          "button",
          { className: "btn btn-primary d-flex align-items-center px-4 py-2", onClick: this.useExample },
          React.createElement("i", { className: "fas fa-plus-circle me-2 fs-5" }),
          "Use token Example"
        ),
        React.createElement(
          "button",
          { className: "btn btn-danger d-flex align-items-center px-4 py-2", onClick: this.clearAll },
          React.createElement("i", { className: "fas fa-trash me-2 fs-5" }),
          "Reset"
        )
      ),
      React.createElement(
        "div",
        { className: "row justify-content-center" },
        React.createElement(
          "div",
          { className: "col-lg-6 col-md-8 col-sm-10 bg-light p-4 rounded shadow" },
          React.createElement("bc-token-printer", {
            biscuit: this.state.token,
            showattenuation: true
          })
        )
      )
    );
  }
}