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
		config: {
			type: "jsonobjectcode",
			props: { label: "Configuration" },
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
		">>>Configuration",
		"config",
		"<<<Token generator",
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
				defaultValue: () => ({
					id: "biscuit_token_" + uuid(),
					name: "Biscuit Token",
					description: "A simple Biscuit Token",
					tags: [],
					metadata: {},
					keypair_ref: "",
					config: {
						facts: [],
						resources: [],
						checks: [],
						rules: [],
					},
				}),
				itemName: "Biscuit Token",
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
		token: this.props?.rawValue?.token || null,
	};

	generateNewToken = () => {
		if (this.props?.rawValue?.keypair_ref && this.props?.rawValue?.config) {
			fetch(
				"/extensions/cloud-apim/extensions/biscuit/tokens/forge/_generate",
				{
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
				}
			)
				.then((d) => d.json())
				.then((data) => {
					this.setState({ token: data.token });
					this.props.changeValue("token", data.token);
				});
		} else {
			console.error("no config and no keypair ref provided !");
		}
	};

	copyToken = () => {
		if (navigator.clipboard && this.state.token) {
			navigator.clipboard.writeText(this.state.token);
		}
	};

	render() {
		if (!this.props.rawValue.keypair_ref) {
			React.createElement(
				"div",
				{ className: "row mb-3" },
				"Please select a KeyPair reference"
			);
		}

		return [
			React.createElement(
				"div",
				{ className: "row mb-3" },
				React.createElement(
					"label",
					{ className: "col-xs-12 col-sm-2 col-form-label" },
					"Biscuit Token"
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
							placeholder: "Your Generated token",
							className: "form-control",
							value: this.props.rawValue?.token,
						})
					)
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
					this.state.token &&
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
					React.createElement("span", null, " Generate new token")
				)
			),
		];
	}
}
