class BiscuitKeyPairPage extends Component {
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
    is_public: {
			type: "bool",
			props: { label: "Expose public key" },
		},
		keypair_generator: {
			type: KeyPairGenerator,
		},
		// playground: {
		// 	type: BiscuitPlayground,
		// },
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
			title: "Public Key",
			filterId: "pubKey",
			content: (item) => item.pubKey,
		},
		{
			title: "Created At",
			filterId: "metadata.created_at",
			content: (item) => item?.metadata?.created_at,
		},
		{
			title: "Updated At",
			filterId: "metadata.updated_at",
			content: (item) => item?.metadata?.updated_at || "--",
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
    "<<<Public Key exposition",
    "is_public",
		"<<<KeyPair parameters",
		"keypair_generator",
		// "<<< Biscuit playground",
		// 'playground'
	];

	componentDidMount() {
		this.props.setTitle(`Biscuit KeyPairs`);
	}

	client = BackOfficeServices.apisClient(
		"biscuit.extensions.cloud-apim.com",
		"v1",
		"biscuit-keypairs"
	);

	render() {
		return React.createElement(
			Table,
			{
				parentProps: this.props,
				selfUrl: "extensions/cloud-apim/biscuit/keypairs",
				defaultTitle: "All Biscuit KeyPairs",
				defaultValue: () => this.client.template(),
				itemName: "Biscuit KeyPair",
				formSchema: this.formSchema,
				formFlow: this.formFlow,
				columns: this.columns,
				stayAfterSave: true,
				fetchItems: (paginationState) =>
					this.client.findAll({
						...paginationState,
						fields: [
							"id",
							"name",
							"desc",
							"pubKey",
							"metadata.created_at",
							"metadata.updated_at",
						],
					}),
        updateItem: (e) => {
					if (!e.privKey || !e.pubKey) {
						alert("Public key or private key not provided !");
					} else {
						return this.client.update(e);
					}
				},
				deleteItem: this.client.delete,
				createItem: (e) => {
					if (!e.privKey || !e.pubKey) {
						alert("Public key or private key not provided !");
					} else {
						return this.client.create(e);
					}
				},
				navigateTo: (item) => {
					window.location = `/bo/dashboard/extensions/cloud-apim/biscuit/keypairs/edit/${item.id}`;
				},
				itemUrl: (item) =>
					`/bo/dashboard/extensions/cloud-apim/biscuit/keypairs/edit/${item.id}`,
				showActions: true,
				showLink: true,
				rowNavigation: true,
				extractKey: (item) => item.id,
				export: true,
				kubernetesKind: "BiscuitKeyPair",
			},
			null
		);
	}
}
class KeyPairGenerator extends Component {
	fetchNewKeyPair = () => {
		fetch("/extensions/assets/cloud-apim/extensions/biscuit/keypairs/generate")
			.then((d) => d.json())
			.then((data) => {
				this.props.changeValue("pubKey", data.publickey);
				this.props.changeValue("privKey", data.privateKey);
			});
	};

	generateNewKeyPair = () => {
		if (this.props?.rawValue?.pubKey || this.props?.rawValue?.privKey) {
			if (
				window.confirm(
					"Do you really want to generate new keypair (it will erase the previous)"
				)
			) {
				this.fetchNewKeyPair();
			}
		} else {
			this.fetchNewKeyPair();
		}
	};

	render() {
		return [
			React.createElement(
				"div",
				{ className: "row mb-3" },
				React.createElement(
					"label",
					{ className: "col-xs-12 col-sm-2 col-form-label" },
					"Public Key"
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
						React.createElement("input", {
							type: "text",
							placeholder: "Your public key here",
							className: "form-control",
							value: this.props.rawValue?.pubKey,
							onChange: (e) => this.props.changeValue("pubKey", e.target.value),
						})
					)
				)
			),
			React.createElement(
				"div",
				{ className: "row mb-3" },
				React.createElement(
					"label",
					{ className: "col-xs-12 col-sm-2 col-form-label" },
					"Private key"
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
						React.createElement("input", {
							type: "text",
							placeholder: "Your private key here",
							className: "form-control",
							value: this.props.rawValue?.privKey,
							onChange: (e) =>
								this.props.changeValue("privKey", e.target.value),
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
				React.createElement(
					"button",
					{
						type: "button",
						className: "btn btn-sm btn-success",
						onClick: this.generateNewKeyPair,
					},
					React.createElement("i", { className: "fas fa-rotate-right" }),
					React.createElement("span", null, " Generate new")
				)
			),
		];
	}
}

class BiscuitPlayground extends Component {
	render() {
		return [React.createElement("bc-token-printer", null, "")];
	}
}
