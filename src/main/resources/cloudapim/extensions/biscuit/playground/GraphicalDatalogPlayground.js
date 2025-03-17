class GraphicalDatalogPlayground extends Component {
  constructor(props) {
    super(props);
    this.state = {
      datalogCode: null
    }
  }

  loadCode = () => {
    this.setState({
      datalogCode: `right("/file1", "read");
right("/file2", "read");
right("/file2", "write");
check if operation("read");`
    })
  }

  render() {
    return React.createElement(
      "div",
      { className: "container text-center mt-4" },
      React.createElement(
        "h1",
        { className: "mb-4 fw-bold text-white" },
        React.createElement("i", { className: "fas fa-play-circle me-2" }),
        "Graphical Datalog Playground"
      ),
      React.createElement(
        "div",
        { className: "row mb-3 justify-content-center align-items-center" },
        React.createElement(
          "div",
          { className: "col-12 col-sm-6" },
          React.createElement(
            "div",
            { className: "card shadow-lg border-primary" },
            React.createElement(
              "div",
              { className: "card-header bg-primary text-white" },
              React.createElement("i", { className: "fas fa-cog me-2" }),
              "Settings"
            ),
            React.createElement(
              "button",
              { className: "btn btn-primary d-flex align-items-center px-4 py-2", onClick: this.loadCode },
              React.createElement("i", { className: "fas fa-plus-circle me-2 fs-5" }),
              "Use code Example"
            ),
            React.createElement(
              "div",
              { className: "card-body" },
              React.createElement("bc-datalog-playground", {
                showauthorizer: true,
                showBlocks: true,
              },
                React.createElement("code", { className: "block" }, `"allow if true;"`),
                React.createElement("code", { className: "block" }, this.state?.datalogCode),
              )
            )
          )
        )
      )
    );
  }
}