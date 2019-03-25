define ([], function () {

    var grid_name = 'rc_contract_objects_grid'

    return function (data, view) {

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        var is_editable = data.item._can.edit
        
        var is_active = !(data.vc_gis_status == 40 || data.vc_gis_status == 100 || data.vc_gis_status == 110)

        $panel.w2regrid ({

            multiSelect: false,

            name: grid_name,

            show: {
                footer: 1,
                toolbar: true,
                toolbarColumns: false,
                toolbarInput: false,
                toolbarSearch: false,
                toolbarAdd: is_editable && is_active,
                toolbarDelete: is_editable && is_active
            },

            searches: [
                {field: 'id_ctr_status', caption: 'Статус', type: 'enum'
                    , options: {items: data.vc_gis_status.items.filter(function (i) {
                        switch (i.id) {
                            case 10:
                            case 40:
                            case 100:
                            case 110:
                                return true;
                            default:
                                return false;
                    }
                })}},
            ],

            textSearch: 'contains',

            columns: [
                {field: 'building.label', caption: 'Адрес', size: 100},
                {field: 'dt_from', caption: 'Дата начала', size: 20, render: _dt},
                {field: 'dt_to', caption: 'Дата окончания', size: 20, render: _dt},
                {field: 'id_ctr_status', caption: 'Статус', size: 20, voc: data.vc_gis_status}
            ],

            postData: {data: {
                uuid_rc_ctr: $_REQUEST.id
            }},

            url: '/mosgis/_rest/?type=rc_contract_objects',

            onDblClick: !is_editable ? null : function (e) {

                var grid = w2ui [e.target]

                var r = grid.get(e.recid)

                $_SESSION.set('record', r)

                use.block('rc_contract_objects_popup')
            },

            onAdd: $_DO.create_rc_contract_objects,

            onDelete: $_DO.delete_rc_contract_objects,
        })

    }

})