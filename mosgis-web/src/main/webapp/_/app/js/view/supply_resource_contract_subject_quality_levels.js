define ([], function () {

    var grid_name = 'supply_resource_contract_subject_quality_levels_grid'

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
                toolbarAdd: is_editable
            },

            textSearch: 'contains',

            columns: [
                {field: 'vc_nsi_276.label', caption: 'Наименование показателя', size: 200},
                {field: 'value', caption: 'Установленное значение показателя качества', size: 50, render: function(i){
                    switch (i['vc_nsi_276.id_type']) {
                        case '3':
                            return i.indicatorvalue_is == 1 ? 'cоответствует' : 'не соответствует'
                        case '2':
                            return i.indicatorvalue == null ? '' : w2utils.formatNumber(i.indicatorvalue)
                        case '1':
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
                uuid_sr_ctr: $_REQUEST.id
            }},

            url: '/mosgis/_rest/?type=supply_resource_contract_quality_levels',

            onDblClick: $_DO.edit_supply_resource_contract_subject_quality_levels,

            onDelete: $_DO.delete_supply_resource_contract_subject_quality_levels,

            onAdd: $_DO.create_supply_resource_contract_subject_quality_levels
        })

    }

})