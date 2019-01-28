define ([], function () {

    var form_name = 'infrastructure_common_form'
    var grid_name = 'code_vc_nsi_3_grid'

    return function (data, view) {

        function recalc () {

            var elements = [
                'independentsource',
                'code_vc_nsi_34',
                'code_vc_nsi_35',
                'code_vc_nsi_40',
                'code_vc_nsi_37',
                'code_vc_nsi_38'
            ]

            var elements_nsi_33 = {
                independentsource: {'1.1':1, '2.1':1, '5.1':1, '5.2':1},
                code_vc_nsi_34:    {'1.1':1},
                code_vc_nsi_35:    {'4.5':1},
                code_vc_nsi_40:    {'2.1':1},
                code_vc_nsi_37:    {'5.2':1},
                code_vc_nsi_38:    {'5.1':1}
            }

            var f = w2ui [form_name]
            var g = w2ui [grid_name]

            elements.forEach ((value, index, array) => {
                if (elements_nsi_33[value][f.record.code_vc_nsi_33.id]) {
                    $('#' + value).closest ('.w2ui-field').show ()
                }
                else {
                    f.record[value] = undefined
                    $('#' + value).closest ('.w2ui-field').hide ()
                }
            })

            if (f.record.indefinitemanagement.id == 1) {
                $('#endmanagmentdate').val ('')
                $('#endmanagmentdate').prop ('placeholder', 'X')
                $('#endmanagmentdate').prop ('disabled', true)
            }
            else if (!$('#name').prop ('disabled')) {
                $('#endmanagmentdate').prop ('disabled', false)
                $('#endmanagmentdate').prop ('placeholder', '')
            }

            if (f.record.code_vc_nsi_33) {
                g.records = dia2w2uiRecords (data.ref_33_to_3.find (x => x.code_33 == f.record.code_vc_nsi_33.id).code_3)
                g.refresh ()
            }

        }
    
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)
            
            var f = w2ui [form_name]

            f.record = r
            
            $('div[data-block-name=infrastructure_common] input, textarea').prop ({disabled: data.__read_only})

            f.refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'main', size: 460}
                
            ]         

        });
        
        var $panel = $(w2ui ['passport_layout'].el ('main'))
                
        fill (view, data.item, $panel)
        
        $panel.w2reform ({ 
        
            name   : form_name,
            
            record : data.item,                
            
            fields : [                     
                    {name: 'name', type: 'text'},
                    {name: 'manageroki', type: 'hidden'},
                    {name: 'manageroki_label', type: 'text'},
                    {name: 'code_vc_nsi_39', type: 'list', options: {items: data.vc_nsi_39.items}},
                    {name: 'indefinitemanagement', type: 'list', options: {items: [
                        {id: "0", text: "Нет"},
                        {id: "1", text: "Да"},
                    ]}},
                    {name: 'endmanagmentdate', type: 'date'},
                    {name: 'code_vc_nsi_33', type: 'list', options: {items: data.vc_nsi_33.items}},
                    {name: 'independentsource', type: 'list', options: {items: [
                        {id: '0', text: 'Нет'},
                        {id: '1', text: 'Да'}
                    ]}},
                    {name: 'code_vc_nsi_34', type: 'list', options: {items: data.vc_nsi_34.items}},
                    {name: 'code_vc_nsi_35', type: 'list', options: {items: data.vc_nsi_35.items}},
                    {name: 'code_vc_nsi_40', type: 'list', options: {items: data.vc_nsi_40.items}},
                    {name: 'code_vc_nsi_37', type: 'list', options: {items: data.vc_nsi_37.items}},
                    {name: 'code_vc_nsi_38', type: 'list', options: {items: data.vc_nsi_38.items}},
                    {name: 'oktmo', type: 'hidden'},
                    {name: 'oktmo_code', type: 'text'},
                    {name: 'adress', type: 'text'},
                    {name: 'comissioningyear', type: 'text'},
                    {name: 'countaccidents', type: 'text'},
                    {name: 'deterioration', type: 'text'},
                    {name: 'addinfo', type: 'textarea'},

            ],

            focus: -1,
            
            onRefresh: function (e) {e.done (setTimeout (function () {
                clickOn ($('#manageroki_label'), $_DO.open_orgs_infrastructure_popup)
                clickOn ($('#oktmo_code'), $_DO.open_oktmo_popup)
                recalc ()
            }, 100))},

            onChange: function (e) {
                if (e.target == 'code_vc_nsi_33' || e.target == 'indefinitemanagement') e.done (recalc)
            }

        })

        $('#type_of_utility_container').w2regrid ({ 
        
            name: grid_name,
            
            show: {
                toolbar: false,
                footer: false,
                columnHeaders: false,
                selectColumn: true
            },     
            
            columns: [
                {field: 'label', caption: 'Наименование', size: 50},
            ],
            
            records: [],

            onRefresh: function () {
                
                var grid = this

                grid.records = dia2w2uiRecords (data.ref_33_to_3.find (x => x.code_33 == w2ui[form_name].record.code_vc_nsi_33.id).code_3)
           
                $.each (data.item.codes_nsi_3, function () {grid.select ('' + this)})
            
            },

            onSelect: function (e) { if ($('#name').prop ('disabled')) return e.preventDefault () },
            onUnselect: function (e) { if ($('#name').prop ('disabled')) return e.preventDefault () }
        
        }).refresh ()

        $_F5 (data)        

    }
    
})