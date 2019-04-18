define ([], function () {

    var form_name = 'citizen_compensation_to_categories_popup_form'
    var grid_name = 'vc_svc_types_grid'

    function recalc () {
        var data = clone ($('body').data ('data'))
        var f = w2ui [form_name]
        var g = w2ui [grid_name]

        if (f.record.uuid_cit_comp_cat) {
            g.records = dia2w2uiRecords (
                data.vc_svc_types.items.filter (x => x['calc_kind.uuid_cit_comp_cat'] == f.record.uuid_cit_comp_cat.id)
                || []
            )

            g.refresh ()
        }

    }

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: form_name,

                record: data.record,

                fields : [
                    {name: 'uuid_cit_comp_cat', type: 'list', options: {items: data.tb_cit_comp_cats.items}},
                    {name: 'periodfrom', type: 'date'},
                    {name: 'periodto', type: 'date'},
                ],

                onRender: function (e) { e.done (setTimeout (recalc, 100)) },

                onChange: function (e) {

                    if (e.target == 'uuid_cit_comp_cat') e.done (function () {
                        recalc () 
                        this.refresh ()
                    })

                },

                focus: data.record.id? -1 : 0
            })

            $('#vc_svc_types_container').w2regrid ({
            
                name: grid_name,

                multiSelect: true,
                
                show: {
                    toolbar: false,
                    footer: false,
                    columnHeaders: false,
                    selectColumn: true,
                },
                
                columns: [
                    {field: 'label', caption: 'Наименование', size: 50},
                ],
                
                records: [],

                onRefresh: function () {
                
                    var grid = this

                    $.each (data.record.vc_svc_types, function () {grid.select ('' + this)})
                
                },

            }).refresh()

       })

    }

})