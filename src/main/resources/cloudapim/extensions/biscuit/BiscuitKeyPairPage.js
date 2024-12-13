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
		keypair_generator: {
			type: KeyPairGenerator,
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
			title: "Public Key",
			filterId: "pubKey",
			content: (item) => item.pubKey,
		},
	];

	formFlow = [
		"_loc",
		"id",
		"name",
		"description",
		"tags",
		"metadata",
		"<<<KeyPair parameters",
		"keypair_generator",
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
				defaultValue: () => ({
					id: "biscuit_keypair_" + uuid(),
					name: "Biscuit Key Pair",
					description: "A simple ED25519 Biscuit KeyPair",
					tags: [],
					metadata: {},
				}),
				itemName: "Biscuit KeyPair",
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
	generateNewKeyPair = () => {
		fetch("/extensions/assets/cloud-apim/extensions/biscuit/keypairs/generate")
			.then((d) => d.json())
			.then((data) => {
				this.props.changeValue("pubKey", data.publickey);
				this.props.changeValue("privKey", data.privateKey);
			});
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
