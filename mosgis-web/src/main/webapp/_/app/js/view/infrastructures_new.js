define ([], function () {

    form_name = 'infrastructure_form'

    function recalc () {

        var $endmanagmentdate = $('#endmanagmentdate')
        var $manageroki_label = $('#manageroki_label')
        var $manageroki = $('#manageroki')

        var r = w2ui [form_name].record

        if ($_USER.uuid_org && !$manageroki.val ()) {
            $manageroki_label.val ($_USER.label_org)
            $manageroki.val ($_USER.uuid_org)
            $manageroki.change ()
        }

        if (r.indefinetemanagement.id) {
            $endmanagmentdate.prop ('disabled', true)
            $endmanagmentdate.prop ('placeholder', 'Управление бессрочно')
        }
        else {
            $endmanagmentdate.prop ('disabled', false)
            $endmanagmentdate.prop ('placeholder', '')
        }

        if ($_USER.role.nsi_20_2) $manageroki_label.prop ('disabled', true)

    }

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'infrastructure_form',

                record: data.record,

                fields : [
                    {name: 'name', type: 'text'},
                    {name: 'manageroki', type: 'hidden'},
                    {name: 'manageroki_label', type: 'text'},
                    {name: 'code_vc_nsi_39', type: 'list', options: {items: data.vc_nsi_39.items}},
                    {name: 'indefinetemanagement', type: 'list', options: {items: [
                        {id: 0, text: 'Нет'},
                        {id: 1, text: 'Да'}
                    ]}},
                    {name: 'endmanagmentdate', type: 'date'},
                    {name: 'code_vc_nsi_33', type: 'list', options: {items: data.vc_nsi_33.items}}
                ],

                onRender: function (e) { e.done (setTimeout (recalc, 100)) },

                onChange: function (e) {

                    if (e.target == 'indefinetemanagement') e.done (function () { 
                        recalc () 
                        this.refresh ()
                    })

                },

                onRefresh: function (e) {e.done (function () {
                
                    clickOn ($('#manageroki_label'), $_DO.open_orgs_infrastructure_popup)
                
                })}

            })

            var is_virgin = 1

            $('#type_of_utility_container').w2regrid ({ 
            
                name: 'code_vc_nsi_3_grid',
                
                show: {
                    toolbar: false,
                    footer: false,
                    columnHeaders: false,
                    selectColumn: true
                },     
                
                columns: [
                    {field: 'label', caption: 'Наименование', size: 50},
                ],
                
                records: dia2w2uiRecords (data.vc_nsi_3.items),

                onRefresh: function () {
                
                    if (!is_virgin) return
                    
                    var grid = this
               
                    $.each (data.record.codes_nsi_3, function () {grid.select ('' + this)})

                    is_virgin = 0
                
                }    
            
            }).refresh ()

       })

    }

})