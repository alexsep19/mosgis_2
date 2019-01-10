define ([], function () {

    var form_name = 'voc_organization_proposal_branch_form'

    $_DO.cancel_voc_organization_proposal_branch = function (e) {

        if (!confirm('Отменить несохранённые правки?'))
            return

        var data = w2ui [form_name].record

        query({type: 'voc_organization_proposals'}, {}, function (data) {

            data.__read_only = true

            $_F5(data)

        })

    }

    $_DO.edit_voc_organization_proposal_branch = function (e) {

        var data = {item: w2ui [form_name].record}

//        if (data.item.id_status == 10)
//            die('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false

        var $form = w2ui [form_name]

        $_F5(data)

    }
    
    $_DO.approve_voc_organization_proposal_branch = function (e) {
        if (!confirm ('Разместить эти данные в ГИС ЖКХ?')) return
        query ({type: 'voc_organization_proposals', action: 'approve'}, {}, reload_page)
    }

    $_DO.update_voc_organization_proposal_branch = function (e) {

        if (!confirm('Сохранить изменения?'))
            return

        var f = w2ui [form_name]

        var v = f.values()

        if (!v.fullname)
            die('fullname', 'Укажите, пожалуйста, полное наименование')
        if (!v.shortname)
            die('shortname', 'Укажите, пожалуйста, сокращенное наименование')

        if (!v.stateregistrationdate)
            die('stateregistrationdate', 'Укажите, пожалуйста, дату государственной регистрации')

        if (!valid_kpp(v.kpp))
            die('kpp', 'Укажите, пожалуйста КПП (9 цифр)')

        if (!/^\d{5}$/.test(v.okopf))
            die('okopf', 'Укажите, пожалуйста ОКОПФ(5 цифр)')

        var r = f.record;

        return query({type: 'voc_organization_proposals', id: r.uuid, action: 'update'}, {data: v}, reload_page)
    }

    $_DO.delete_voc_organization_proposal_branch = function (e) {
        if (!confirm('Удалить эту запись, Вы уверены?'))
            return
        query({type: 'voc_organization_proposals', action: 'delete'}, {}, reload_page)
    }

    $_DO.choose_tab_voc_organization_proposal_branch = function (e) {

        var name = e.tab.id

        var layout = w2ui ['topmost_layout']

        if (layout) {
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('voc_organization_proposal_branch.active_tab', name)

        use.block (name)
    }

    return function (done) {

        query ({type: 'voc_organization_proposals'}, {}, function (data) {
        
            var it = data.item

            it._can = {cancel: 1}
            
            if ($_USER.role.admin) {
            
                switch (it.id_org_pr_status) {
                    case 10:
                        it._can.edit = 1
                        it._can.approve = 1
                }
            
            }
                        
            it._can.update = it._can.delete = it._can.edit
                        
            $('body').data ('data', data)

            done(data)

        })

    }

})