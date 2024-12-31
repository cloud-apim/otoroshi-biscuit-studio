class BiscuitRemoteFactsLoaderPage extends Component {
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
        'config.apiUrl': {
            type: "string",
            props: {
                label: "API URL",
                placeholder: "Description of the Context",
            },
        },
        'config.headers': {
                    type: "object",
         			props: { label: "Headers" },
        }
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
		"<<<Configuration",
		"config.apiUrl",
		"config.headers"
	];

	componentDidMount() {
		this.props.setTitle(`Biscuit Remote Facts Loader`);
	}

	client = BackOfficeServices.apisClient(
		"biscuit.extensions.cloud-apim.com",
		"v1",
		"remote-facts"
	);

	render() {
		return React.createElement(
			Table,
			{
				parentProps: this.props,
				selfUrl: "extensions/cloud-apim/biscuit/remote-facts",
				defaultTitle: "All Biscuit Remote Facts",
				defaultValue: () => ({
					id: "biscuit-remote-facts_" + uuid(),
					name: "Remote fact loader",
					description: "Biscuit Remote fact loader",
					tags: [],
					metadata: {},
					config:{
					    apiUrl: "https://api.domain.com/v1/roles",
					    headers: {
					        "Accept": "application/json",
					        "Authorization": "Bearer: xxxxx"
					    }
					}
				}),
				itemName: "Biscuit Remote Facts Loader",
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
					window.location = `/bo/dashboard/extensions/cloud-apim/biscuit/remote-facts/edit/${item.id}`;
				},
				itemUrl: (item) =>
					`/bo/dashboard/extensions/cloud-apim/biscuit/remote-facts/edit/${item.id}`,
				showActions: true,
				showLink: true,
				rowNavigation: true,
				extractKey: (item) => item.id,
				export: true,
				kubernetesKind: "BiscuitRemoteFactsLoader",
			},
			null
		);
	}
}
