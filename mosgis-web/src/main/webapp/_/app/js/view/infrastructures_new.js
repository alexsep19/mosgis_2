define ([], function () {

    form_name = 'infrastructure_form'

    function recalc () {

        var $endmanagmentdate = $('#endmanagmentdate')
        var $manageroki_label = $('#manageroki_label')

        var r = w2ui [form_name].record

        if (r.indefinetemanagement.id) {
            $endmanagmentdate.prop ('disabled', true)
            $endmanagmentdate.prop ('placeholder', 'Управление бессрочно')
        }
        else {
            $endmanagmentdate.prop ('disabled', false)
            $endmanagmentdate.prop ('placeholder', '')
        }

        if ($_USER.role.nsi_20_2) {
            $manageroki_label.prop ('disabled', true)
            $manageroki_label.prop ('placeholder', $_USER.label_org)
        }

    }

    return function (data, view) {

        $(fill (view, {})).w2uppop ({}, function () {

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

                }

            })

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
                
                records: dia2w2uiRecords (data.vc_nsi_3.items)            
            
            }).refresh ()

       })

    }

})