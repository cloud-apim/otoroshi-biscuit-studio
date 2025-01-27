class BiscuitTokenForge extends Component {
	formSchema = {
		_loc: {
			type: "location",
			props: {},
		},
		id: {
			type: "string",
			disabled: true,
			props: { label: "Id", placeholder: "---" },
		},
		name: {
			type: "string",
			props: { label: "Name", placeholder: "My Awesome Context" },
		},
		description: {
			type: "string",
			props: {
				label: "Description",
				placeholder: "Description of the Context",
			},
		},
		metadata: {
			type: "object",
			props: { label: "Metadata" },
		},
		tags: {
			type: "array",
			props: { label: "Tags" },
		},
		keypair_ref: {
			type: "select",
			props: {
				label: "Key Pair Reference",
				valuesFrom:
					"/bo/api/proxy/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-keypairs",
				transformer: (item) => ({ label: item.name, value: item.id }),
			},
		},
		"config.facts": {
			type: "array",
			props: { label: "Facts" },
		},
		"config.checks": {
			type: "array",
			props: { label: "Checks" },
		},
		"config.resources": {
			type: "array",
			props: { label: "Resources" },
		},
		"config.rules": {
			type: "array",
			props: { label: "Rules" },
		},
		tokengen: {
			type: TokenGenerator,
		},
	};

	columns = [
		{
			title: "Name",
			filterId: "name",
			content: (item) => item.name,
		},
		{
			title: "Description",
			filterId: "description",
			content: (item) => item.description,
		},
		{
			title: "Created At",
			filterId: "metadata.created_at",
			content: (item) => item?.metadata?.created_at,
		},
	];

	formFlow = [
		"_loc",
		"id",
		"name",
		"description",
		">>>Metadata and tags",
		"tags",
		"metadata",
		"<<<KeyPair",
		"keypair_ref",
		">>>Facts",
		"config.facts",
		">>>Checks",
		"config.checks",
		">>>Resources",
		"config.resources",
		">>>Rules",
		"config.rules",
		">>>Test Token generator",
		"tokengen",
	];

	componentDidMount() {
		this.props.setTitle(`Tokens Forge`);
	}

	client = BackOfficeServices.apisClient(
		"biscuit.extensions.cloud-apim.com",
		"v1",
		"tokens-forge"
	);

	render() {
		return React.createElement(
			Table,
			{
				parentProps: this.props,
				selfUrl: "extensions/cloud-apim/biscuit/tokens-forge",
				defaultTitle: "Tokens forge",
				defaultValue: () => this.client.template(),
				itemName: "Tokens Forge",
				formSchema: this.formSchema,
				formFlow: this.formFlow,
				columns: this.columns,
				stayAfterSave: true,
				fetchTemplate: () => this.client.template(),
				fetchItems: (paginationState) => this.client.findAll(),
				updateItem: this.client.update,
				deleteItem: this.client.delete,
				createItem: this.client.create,
				navigateTo: (item) => {
					window.location = `/bo/dashboard/extensions/cloud-apim/biscuit/tokens-forge/edit/${item.id}`;
				},
				itemUrl: (item) =>
					`/bo/dashboard/extensions/cloud-apim/biscuit/tokens-forge/edit/${item.id}`,
				showActions: true,
				showLink: true,
				rowNavigation: true,
				extractKey: (item) => item.id,
				export: true,
				kubernetesKind: "BiscuitTokenForge",
			},
			null
		);
	}
}

class TokenGenerator extends Component {
	state = {
		token: null,
		errorMessage: null,
	};

	generateNewToken = () => {
		if (this.props?.rawValue?.keypair_ref && this.props?.rawValue?.config) {
			fetch("/extensions/cloud-apim/extensions/biscuit/tokens/_generate", {
				method: "POST",
				credentials: "include",
				headers: {
					Accept: "application/json",
					"Content-Type": "application/json",
				},
				body: JSON.stringify({
					keypair_ref: this.props.rawValue.keypair_ref,
					config: this.props.rawValue.config,
				}),
			})
				.then((d) => d.json())
				.then((data) => {
					if (!data?.done) {
						this.setState({
							error: `Generation error : ${data.error}`,
						});
					} else {
						this.setState({ token: data.token, errorMessage: null });
					}
				});
		} else {
			this.setState({
				errorMessage: `no config and no keypair ref provided !`,
			});
		}
	};

	copyToken = () => {
		if (navigator.clipboard && this.state.token) {
			navigator.clipboard.writeText(this.state.token);
		}
	};

	render() {
		const { errorMessage, token } = this.state;

		if (!this.props?.rawValue?.keypair_ref) {
			return [
				React.createElement(
					"div",
					{ className: "row mb-3" },
					React.createElement("div", {
						className: "col-xs-12 col-sm-2 col-form-label",
					}),
					React.createElement(
						"label",
						{ className: "col-sm-10", style: { display: "flex" } },
						"Please select a KeyPair reference before generating a new token"
					)
				),
			];
		}

		return [
			React.createElement(
				"div",
				{ className: "row mb-3" },
				React.createElement(
					"label",
					{ className: "col-xs-12 col-sm-2 col-form-label" },
					"Generate biscuit test token"
				),
				React.createElement(
					"div",
					{ className: "col-sm-10", style: { display: "flex" } },
					React.createElement(
						"div",
						{
							style: {
								display: "flex",
								width: "100%",
								flexDirection: "column",
							},
						},
						React.createElement("textarea", {
							ref: (r) => (this.ref = r),
							type: "text",
							rows: 5,
							disabled: true,
							placeholder: "Your new generated token will be displayed here",
							className: "form-control",
							value: token,
						})
					)
				)
			),

			errorMessage &&
				React.createElement(
					"div",
					{
						style: { maxWidth: "80%", marginLeft: "15%", textAlign: "center" },
					},
					React.createElement(
						"div",
						{
							className: "alert alert-danger rounded mx-auto",
							style: { width: "100%", textAlign: "center" },
						},
						React.createElement("i", {
							className: "fas fa-exclamation-circle",
						}),
						React.createElement("span", null, ` ${errorMessage}`)
					)
				),
			React.createElement(
				"div",
				{ className: "mb-3" },
				React.createElement(
					"label",
					{ className: "col-xs-12 col-sm-2 col-form-label" },
					""
				),
				navigator.clipboard &&
					token &&
					React.createElement(
						"button",
						{
							type: "button",
							className: "btn btn-sm btn-primary",
							onClick: this.copyToken,
						},
						React.createElement("i", { className: "fas fa-clipboard" }),
						React.createElement("span", null, " Copy token to clipboard")
					),
				React.createElement(
					"button",
					{
						type: "button",
						className: "btn btn-sm btn-success",
						onClick: this.generateNewToken,
					},
					React.createElement("i", { className: "fas fa-rotate-right" }),
					React.createElement(
						"span",
						{ disabled: this?.props?.rawValue?.keypair_ref },
						"Generate new test token"
					)
				)
			),
		];
	}
}
