define ([], function () {

    var grid_name = 'organization_member_property_documents_grid'

    return function (data, view) {

        var is_author = $_USER.role.nsi_20_1 || $_USER.is_building_society ()

        var postData = {data: {}}
        if (is_author)
                postData.data.uuid_org = $_USER.uuid_org

        if (data.item.uuid_org_member) {
            postData.data.uuid_org_owner = data.item.uuid_org_member
        }
        if (data.item.uuid_person_member) {
            postData.data.uuid_person_owner = data.item.uuid_person_member
        }

        data._can = {
            create_person: data.item.uuid_person_member && ($_USER.role.admin || $_USER.uuid_org == data.item.uuid_org),
            create_org: data.item.uuid_org_member && ($_USER.role.admin || $_USER.uuid_org == data.item.uuid_org)
        }

        $(w2ui ['organization_member_common_layout'].el('main')).w2regrid({

            toolbar: {

                items: [
                    {type: 'button', id: 'create_person', caption: 'Физическое лицо', icon: 'w2ui-icon-plus'
                        , onClick: $_DO.create_person_organization_member_property_documents
                        , off: !data._can.create_person
                    },
                    {type: 'button', id: 'create_org', caption: 'Юридическое лицо', icon: 'w2ui-icon-plus'
                        , onClick: $_DO.create_org_organization_member_property_documents
                        , off: !data._can.create_org
                    },
                ].filter (not_off),

            },

            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: 1,
            },

            textSearch: 'contains',

            searches: [
                {field: 'label',  caption: 'Номер помещения',  type: 'text'},
                {field: 'owner_label_uc',  caption: 'Собственник',  type: 'text'},
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}},
            ].filter (not_off),

            columns: [
                {field: 'label', caption: 'Помещение', size: 10},
                {field: 'owner_label', caption: 'Собственник', size: 30},
                {field: 'totalarea', caption: 'Площадь, м2', size: 10},
                {field: 'prc', caption: 'Доля, %', size: 10},
                {field: 'id_type', caption: 'Документ', size: 25, voc: data.vc_prop_doc_types},
                {field: 'no', caption: '№', size: 25},
                {field: 'dt', caption: 'Дата', size: 18, render:_dt},
                {field: 'author_label', caption: 'Поставщик информации', size: 30, off: is_author},
            ].filter (not_off),

            postData: postData,

            url: '/mosgis/_rest/?type=property_documents',

            onDblClick: function (e) {

                var c = this.columns [e.column];

                if (!c.editable)
                    openTab ('/property_document/' + e.recid)
            }

        })

    }

})