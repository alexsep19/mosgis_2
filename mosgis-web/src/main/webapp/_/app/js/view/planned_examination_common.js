define ([], function () {
    
    var form_name = 'planned_examination_common_form'
    
    return function (data, view) {
        
        function recalc () {

            $('#uriregistrationnumber').prop ('disabled', true)
            $('#uriregistrationdate').prop ('disabled', true)

            var r = w2ui [form_name].record

            if (!r.shouldberegistered) {

                $('#uriregistrationnumber').prop ('placeholder', 'Проверка не должна быть зарегистрирована в ЕРП')
                $('#uriregistrationdate').prop ('placeholder', 'X')

            }
            else if (!r.__read_only) {
                $('#uriregistrationnumber').prop ('disabled', false)
                $('#uriregistrationdate').prop ('disabled', false)
            }

        }

        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)
            
            var f = w2ui [form_name]
            
            f.record = r
            
            $('div[data-block-name=planned_examination_common] input, textarea').prop ({disabled: data.__read_only})

            $('#regulator_label').prop ({disabled: false, readonly: data.__read_only})
            $('#subject_label').prop ({disabled: false, readonly: data.__read_only})

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

                    var $regulator_label = $('#regulator_label')
                    var $subject_label = $('#subject_label')

                    if (!$regulator_label.prop ('readonly')) clickOn ($regulator_label, $_DO.open_regulator_orgs_popup)
                    else clickOn ($regulator_label, $_DO.open_regulator_tab)

                    if (!$subject_label.prop ('readonly')) clickOn ($subject_label, $_DO.open_subject_orgs_popup)
                    else clickOn ($subject_label, $_DO.open_subject_tab)
                    
                    recalc ()
                
            })},

            focus: -1,
            
        })

        $_F5 (data)        

    }
    
})