define ([], function () {

    $_DO.update_voting_protocol_vote_decision_lists_new = function (e) {

        var form = w2ui ['voting_protocol_vote_decision_lists_new_form']

        var v = form.values ()

        if (!v.decisiontype_vc_nsi_63) die ('decisiontype_vc_nsi_63', 'Пожалуйста, выберите тип вопроса из списка')
        if (!v.questionname) die ('questionname', 'Пожалуйста, введите вопрос')

        if (v.decisiontype_vc_nsi_63 == '2.1' && !v.formingfund_vc_nsi_241) die ('formingfund_vc_nsi_241', 'Пожалуйста, укажите способ формирования фонда капитального ремонта')
        if (v.decisiontype_vc_nsi_63 == '11.1' && !v.managementtype_vc_nsi_25) die ('managementtype_vc_nsi_25', 'Пожалуйста, укажите способ управления МКД')

        if (!v.agree && !v.against && !v.abstent) die ('agree', 'Пожалуйста, заполните как минимум одно поле результата голосования')

        if (!v.agree) v['agree'] = 0
        if (!v.against) v['against'] = 0
        if (!v.abstent) v['abstent'] = 0

        if (!v.hasOwnProperty('votingresume')) die ('votingresume', 'Пожалуйста, укажите итог голосования')

        v['votingresume'] = (v['votingresume'] == 0) ? "M" : "N"
        
        var tia = {type: 'vote_decision_lists'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['voting_protocol_vote_decision_lists_grid']
        
        var data = clone ($('body').data ('data'))

        v['protocol_uuid'] = data.item.uuid

        query (tia, {data: v}, function (data) {
        
            w2popup.close ()

            if (data.id) w2confirm ('Перейти на страницу повестки?').yes (function () {openTab ('/vote_decision_lists/' + data.id)})
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})