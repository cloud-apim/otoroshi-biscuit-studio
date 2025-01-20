class BiscuitAttenuatorPage extends Component {
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
		"config.checks": {
			type: "array",
			props: { label: "Checks" },
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
		"tags",
		"metadata",
		"keypair_ref",
		"<<<Checks",
		"config.checks",
	];

	componentDidMount() {
		this.props.setTitle(`Biscuit Attenuators`);
	}

	client = BackOfficeServices.apisClient(
		"biscuit.extensions.cloud-apim.com",
		"v1",
		"biscuit-attenuators"
	);

	render() {
		return React.createElement(
			Table,
			{
				parentProps: this.props,
				selfUrl: "extensions/cloud-apim/biscuit/attenuators",
				defaultTitle: "All Biscuit Attenuators",
				defaultValue: () => this.client.template(),
				itemName: "Biscuit Attenuator",
				formSchema: this.formSchema,
				formFlow: this.formFlow,
				columns: this.columns,
				stayAfterSave: true,
				fetchTemplate: () => this.client.template(),
				fetchItems: (paginationState) => this.client.findAll(),
				updateItem: (e) => {
					if (!e.keypair_ref) {
						alert(
							"Could not update entity if the keypair reference is not provided"
						);
					} else if (e?.checks?.length === 0) {
						alert("Your checks array is empty, please add check rules");
					} else {
						return this.client.update(e);
					}
				},
				deleteItem: this.client.delete,
				createItem: (e) => {
					if (!e.keypair_ref) {
						alert(
							"Could not create entity if the keypair reference is not provided"
						);
					} else if (e?.checks?.length === 0) {
						alert("Your checks array is empty, please add check rules");
					} else {
						return this.client.create(e);
					}
				},
				navigateTo: (item) => {
					window.location = `/bo/dashboard/extensions/cloud-apim/biscuit/attenuators/edit/${item.id}`;
				},
				itemUrl: (item) =>
					`/bo/dashboard/extensions/cloud-apim/biscuit/attenuators/edit/${item.id}`,
				showActions: true,
				showLink: true,
				rowNavigation: true,
				extractKey: (item) => item.id,
				export: true,
				kubernetesKind: "BiscuitAttenuator",
			},
			null
		);
	}
}
