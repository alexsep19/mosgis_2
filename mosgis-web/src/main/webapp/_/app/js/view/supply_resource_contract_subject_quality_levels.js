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
                toolbar: false,
                toolbarColumns: false,
                toolbarInput: false,
                toolbarReload: false,
                toolbarDelete: false,
                toolbarAdd: false
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

            onLoad: function (e) {

                if (e.xhr.status != 200) return $_DO.apologize ({jqXHR: e.xhr})

                var content = JSON.parse (e.xhr.responseText).content

                var data = {
                    status : "success",
                    total  : content.cnt
                }

                delete content.cnt
                delete content.portion

                var data = $('body').data('data')

                for (key in content) {

                    var rs = content [key]

                    var seen = {}

                    $.each (rs, function () {
                        seen [this.code_vc_nsi_276] = this
                    })

                    var k = -1;

                    $.each(data.vc_nsi_276.items, function () {
                        if(!seen [this.id]) {
                            rs.push({
                                id: k--,
                                uuid_sr_ctr_subj: data.item.uuid,
                                code_vc_nsi_276: this.id,
                                vc_nsi_276: this,
                                'vc_nsi_276.label': this.label
                            })
                        }
                    })

                    rs = rs.sort(function (a, b) {
                        return a['vc_nsi_276.label'] < b['vc_nsi_276.label'] ? -1
                            : a['vc_nsi_276.label']  > b['vc_nsi_276.label']  ? 1 : 0
                    })

                    data.records = dia2w2uiRecords(rs)

                    e.xhr.responseText = JSON.stringify (data)
                }
            }
        })

    }

})