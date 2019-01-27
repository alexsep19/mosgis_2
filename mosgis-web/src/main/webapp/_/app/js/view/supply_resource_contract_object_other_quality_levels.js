define ([], function () {

    var grid_name = 'supply_resource_contract_object_other_quality_levels_grid'

    return function (data, view) {

        var layout = w2ui ['passport_layout']

        var $panel = $(layout.el ('main'))

        var is_editable = data.item._can.edit

        $panel.w2regrid ({

            multiSelect: false,

            name: grid_name,

            show: {
                footer: 1,
                toolbar: true,
                toolbarColumns: false,
                toolbarInput: false,
                toolbarReload: false,
                toolbarDelete: is_editable,
                toolbarAdd: false
            },

            textSearch: 'contains',
            toolbar: {
                onClick: function (e) {
                    if (/^create/.test(e.item.id) && e.subItem) {
                        $_SESSION.set('record', {id_type: e.subItem.id, uuid_sr_ctr_subj: data.item.uuid})
                        use.block('supply_resource_contract_object_other_quality_levels_popup')
                    }
                },
                items: !is_editable ? [] : [
                    {
                        id: 'create',
                        type: 'menu',
                        text: 'Добавить',
                        icon: 'w2ui-icon-plus',
                        selected: -1,
                        items: data.vc_gis_sr_ql_types.items
                    }
                ].filter(not_off),
            },

            columns: [
                {field: 'label', caption: 'Наименование показателя', size: 200},
                {field: 'value', caption: 'Установленное значение показателя качества', size: 50, render: function(i){
                    switch (i.id_type) {
                        case 3:
                            return i.indicatorvalue_is == 1 ? 'cоответствует' : 'не соответствует'
                        case 2:
                            return i.indicatorvalue == null ? '' : w2utils.formatNumber(i.indicatorvalue)
                        case 1:
                            return (i.indicatorvalue_from? ('от ' + w2utils.formatNumber(i.indicatorvalue_from)) : '')
                                + (i.indicatorvalue_to? (' до ' + w2utils.formatNumber(i.indicatorvalue_to)) : '')
                                + (i['okei.national'] ? (' ' + i['okei.national']) : '')
                        default:
                            return null
                    }
                }},
                {field: 'additionalinformation', caption: 'Дополнительная информация', size: 50},
            ],

            postData: {data: {
                uuid_sr_ctr_obj: $_REQUEST.id
            }},

            url: '/mosgis/_rest/?type=supply_resource_contract_object_other_quality_levels',

            onDblClick: $_DO.edit_supply_resource_contract_object_other_quality_levels,

            onDelete: $_DO.delete_supply_resource_contract_object_other_quality_levels
        })

    }

})