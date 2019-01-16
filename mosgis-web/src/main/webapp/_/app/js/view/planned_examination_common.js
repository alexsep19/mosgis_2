define ([], function () {
    
    var form_name = 'planned_examination_common_form'
    
    return function (data, view) {
        
        function recalc () {

            $('#uriregistrationplannumber').prop ('disabled', true)
            $('#uriregistrationplandate').prop ('disabled', true)

            var r = w2ui [form_name].record

            if (!r.shouldberegistered) {

                $('#uriregistrationplannumber').prop ('placeholder', 'Проверка не должна быть зарегистрирована в ЕРП')
                $('#uriregistrationplandate').prop ('placeholder', 'Проверка не должна быть зарегистрирована в ЕРП')

            }
            else if (!r.__read_only) {
                $('#uriregistrationplannumber').prop ('disabled', false)
                $('#uriregistrationplandate').prop ('disabled', false)
            }

        }

        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)
            
            var f = w2ui [form_name]
            
            f.record = r
            
            $('div[data-block-name=planned_examination_common] input, textarea').prop ({disabled: data.__read_only})

            //recalc ()

            f.refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({

            name: 'passport_layout',

            panels: [
                {type: 'main'}
            ]

        });

        var $panel = $(w2ui ['passport_layout'].el ('main'))

        fill (view, data.item, $panel) 
                
        $panel.w2reform ({
        
            name   : form_name,
            
            record : data.item,                
            
            fields : [
                {name: 'numberinplan', type: 'text'},
                {name: 'uriregistrationnumber', type: 'text'},
                {name: 'uriregistrationdate', type: 'date'},
                {name: 'code_vc_nsi_65', type: 'list', options: {items: data.vc_nsi_65.items}},
                {name: 'code_vc_nsi_71', type: 'list', options: {items: data.vc_nsi_71.items}},

                {name: 'regulator_uuid', type: 'hidden'},
                {name: 'regulator_label', type: 'text'},
                {name: 'functionregistrynumber', type: 'text'},
                {name: 'authorizedpersons', type: 'textarea'},
                {name: 'involvedexperts', type: 'textarea'},

                {name: 'subject_uuid', type: 'hidden'},
                {name: 'subject_label', type: 'text'},
                {name: 'actualactivityplace', type: 'textarea'},
                {name: 'smallbusiness', type: 'list', options: {items: [
                    {id: 0, text: 'Нет'},
                    {id: 1, text: 'Да'}
                ]}},

                {name: 'code_vc_nsi_68', type: 'list', options: {items: data.vc_nsi_68.items}},
                {name: 'additionalinfoaboutexambase', type: 'textarea'},
                {name: 'objective', type: 'textarea'},
                {name: 'lastexaminationenddate', type: 'date'},
                {name: 'yearfrom', type: 'text'},
                {name: 'monthfrom', type: 'text'},
                {name: 'workdays', type: 'text'},
                {name: 'workhours', type: 'text'},
                {name: 'cooperationwith', type: 'textarea'},
                {name: 'prosecutoragreementinformation', type: 'textarea'}
            ],

            onRefresh: function (e) {e.done (function () {             

                    clickOn ($('#regulator_label'), $_DO.open_regulator_orgs_popup)
                    clickOn ($('#subject_label'), $_DO.open_subject_orgs_popup)
                    recalc ()
                
            })},

            focus: -1,
            
        })

        $_F5 (data)        

    }
    
})