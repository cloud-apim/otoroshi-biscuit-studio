class BiscuitRevocation extends Component {
  state = {
    revokedTokens: [],
    newTokenId: "",
    newReason: "",
    searchTokenId: "",
    showModal: false,
  };

  componentDidMount() {
    this.props.setTitle("Biscuit Revoked Tokens");
    this.fetchRevokedTokens();
  }

  fetchRevokedTokens = () => {
    fetch("/extensions/cloud-apim/extensions/biscuit/tokens/revocation/_all", {
      method: "GET",
      credentials: "include",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
    })
      .then((res) => res.json())
      .then((data) => {
        this.setState({ revokedTokens: data?.tokens || [] });
      })
      .catch((error) => console.error("Error fetching tokens:", error));
  };

  confirmAddToken = () => {
    if(this.state.newTokenId){
      this.setState({ showModal: true });
    }
  };

  addNewToken = () => {
    const { newTokenId, newReason } = this.state;
    if (!newTokenId) return;

    fetch("/extensions/cloud-apim/extensions/biscuit/tokens/_revoke", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ id: newTokenId, reason: newReason, revocation_date: new Date().toISOString() }),
    })
      .then((res) => res.json())
      .then(() => {
        this.fetchRevokedTokens();
        this.setState({ newTokenId: "", newReason: "", showModal: false });
      })
      .catch((error) => console.error("Error adding token:", error));
  };

  handleInputChange = (e) => {
    this.setState({ [e.target.name]: e.target.value });
  };

  closeModal = () => {
    this.setState({ showModal: false });
  };

  render() {
    const { revokedTokens, newTokenId, newReason, searchTokenId, showModal } = this.state;

    const filteredTokens = revokedTokens.filter((token) =>
      token.id.toLowerCase().includes(searchTokenId.toLowerCase())
    );

    return React.createElement("div", { className: "container mt-4 p-4 bg-light rounded shadow" }, [
      React.createElement("h2", { className: "mb-3 text-primary" }, `${revokedTokens.length} Total Revoked Tokens`),
      React.createElement("p", { className: "text-muted" }, "Below is the list of revoked tokens. Once revoked, a token cannot be removed."),
      React.createElement("div", { className: "mb-3" },
        React.createElement("input", {
          type: "text",
          name: "searchTokenId",
          value: searchTokenId,
          onChange: this.handleInputChange,
          placeholder: "Search Token ID",
          className: "form-control"
        })
      ),
      React.createElement(
        "table",
        { className: "table table-hover table-bordered bg-white" },
        React.createElement("thead", { className: "table-primary" },
          React.createElement("tr", null, [
            React.createElement("th", { key: "id" }, "Token ID"),
            React.createElement("th", { key: "reason" }, "Reason"),
            React.createElement("th", { key: "revocation_date" }, "Revocation Date")
          ])
        ),
        React.createElement("tbody", null,
          filteredTokens.map((token) =>
            React.createElement("tr", { key: token.id }, [
              React.createElement("td", null, token.id),
              React.createElement("td", null, token.reason.length > 30 ? token.reason.substring(0, 30) + "..." : token.reason),
              React.createElement("td", null, new Date(token.revocation_date).toLocaleString())
            ])
          )
        )
      ),
      React.createElement("div", { className: "mt-4 row g-3 p-3 bg-white rounded shadow" }, [
        React.createElement("h4", { className: "mb-3" }, "Revoke a New Token"),
        React.createElement("p", { className: "text-muted" }, "Enter the token ID and reason for revocation. Note: This action is irreversible."),
        React.createElement("div", { className: "col" },
          React.createElement("input", {
            type: "text",
            name: "newTokenId",
            value: newTokenId,
            onChange: this.handleInputChange,
            placeholder: "Token ID",
            className: "form-control"
          })
        ),
        React.createElement("div", { className: "col" },
          React.createElement("input", {
            type: "text",
            name: "newReason",
            value: newReason,
            onChange: this.handleInputChange,
            placeholder: "Reason",
            className: "form-control"
          })
        ),
        React.createElement("div", { className: "col-auto" },
          React.createElement("button", {
            disabled: !this.state.newTokenId,
            onClick: this.confirmAddToken,
            className: "btn btn-danger"
          }, "Revoke Token")
        )
      ]),
      showModal && React.createElement("div", { className: "modal fade show d-block", tabIndex: "-1", role: "dialog", style: { backgroundColor: "rgba(0,0,0,0.5)" } },
        React.createElement("div", { className: "modal-dialog modal-dialog-centered", role: "document" },
          React.createElement("div", { className: "modal-content p-4" }, [
            React.createElement("div", { className: "modal-header bg-danger text-white" },
              React.createElement("h5", { className: "modal-title" }, "Confirm Token Revocation"),
              React.createElement("button", { type: "button", className: "btn-close", onClick: this.closeModal })
            ),
            React.createElement("div", { className: "modal-body text-center" },
              React.createElement("p", { className: "fw-bold" }, "Warning: Once revoked, a token cannot be restored."),
              React.createElement("p", null, `Token ID: ${newTokenId}`),
              React.createElement("p", null, `Reason: ${newReason.length > 50 ? newReason.substring(0, 50) + "..." : newReason}`)
            ),
            React.createElement("div", { className: "modal-footer d-flex justify-content-between" }, [
              React.createElement("button", { type: "button", className: "btn btn-secondary", onClick: this.closeModal }, "Cancel"),
              React.createElement("button", { type: "button", className: "btn btn-danger", onClick: this.addNewToken }, "Confirm Revocation")
            ])
          ])
        )
      )
    ]);
  }
}