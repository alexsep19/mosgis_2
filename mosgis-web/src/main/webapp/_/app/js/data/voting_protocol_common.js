define ([], function () {

    var form_name = 'voting_protocol_common_form'

    $_DO.cancel_voting_protocol_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'voting_protocols'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }
    
    $_DO.edit_voting_protocol_common = function (e) {

        var data = {item: w2ui [form_name].record}

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }
    
    $_DO.approve_voting_protocol_common = function (e) {
        if (!confirm ('Разместить эти данные в ГИС ЖКХ?')) return
        query ({type: 'voting_protocols', action: 'approve'}, {}, reload_page)
    }
    
    $_DO.alter_voting_protocol_common = function (e) {
        if (!confirm ('Открыть эту карточку на редактирование?')) return
        query ({type: 'voting_protocols', action: 'alter'}, {}, reload_page)
    }

    $_DO.update_voting_protocol_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()

        console.log ("v:", v)
        
        if (!v.protocoldate) die ('protocoldate', 'Пожалуйста, введите дату составления протокола')
        if (!v.hasOwnProperty('extravoting')) die ('extravoting', 'Пожалуйста, укажите вид собрания')
        if (!v.hasOwnProperty('meetingeligibility')) die ('meetingeligibility', 'Пожалуйста, укажите правомочность собрания')
        if (!v.form_) die ('form_', 'Пожалуйста, выберите форму проведения')

        switch (v.form_) {
            case '0':
                if (!v.avotingstartdate) die ('avotingstartdate', 'Пожалуйста, введите дату начала приема решений')
                if (!v.avotingdate) die ('avotingdate', 'Пожалуйста, введите дату окончания приема решений')
                if ( v.avotingdate < v.avotingstartdate) die ('avotingdate', 'Дата окончания не может предшествовать дате начала')
                if (!v.resolutionplace) die ('resolutionplace', 'Пожалуйста, введите место принятия решений')
                break;
            case '1':
                if (!v.meetingdate) die ('meetingdate', 'Пожалуйста, введите дату проведения собрания')
                if (!v.votingplace) die ('votingplace', 'Пожалуйста, введите место проведения собрания')

                break;
            case '2':
                if (!v.evotingdatebegin) die ('evotingdatebegin', 'Пожалуйста, введите дату начала проведения голосования')
                if (!v.evotingdateend) die ('evotingdateend', 'Пожалуйста введите дату окончания проведения голосования')
                if (!v.discipline) die ('discipline', 'Пожалуйста, введите порядок приема оформленных в письменной форме решений собственников')
                if (!v.inforeview) die ('inforeview', 'Пожалуйста, введите порядок ознакомления с информацией')

                if ((Date.parse (v.evotingdateend) - Date.parse (v.evotingdatebegin)) <= 0) die ('evotingdateend', 'Некорректный временной промежуток')
                break;
            case '3':
                if (!v.meeting_av_date) die ('meeting_av_date', 'Пожалуйста, введите дату и время проведения собрания')
                if (!v.meeting_av_date_start) die ('meeting_av_date_start', 'Пожалуйста, введите дату начала приема решений')
                if (!v.meeting_av_date_end) die ('meeting_av_date_end', 'Пожалуйста, введите дату окончания приема решений')
                if ( v.meeting_av_date_end < v.meeting_av_date_start) die ('meeting_av_date_end', 'Дата окончания не может предшествовать дате начала')
                if (!v.meeting_av_place) die ('meeting_av_place', 'Пожалуйста, введите место проведения собрания')
                if (!v.meeting_av_res_place) die ('meeting_av_res_place', 'Пожалуйста, введите место приема решения')
                
                if ((Date.parse (v.meeting_av_date_end) - Date.parse (v.meeting_av_date)) < 0) die ('meeting_av_date_end', 'Некорректный временной промежуток')
                break;
        }

        query ({type: 'voting_protocols', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_voting_protocol_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'voting_protocols', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.undelete_voting_protocol_common = function (e) {   
        if (!confirm ('Восстановить эту запись, Вы уверены?')) return        
        query ({type: 'voting_protocols', action: 'undelete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_voting_protocol_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('voting_protocol_common.active_tab', name)
            
        use.block (name)        
    
    }

    return function (done) { 

        w2ui ['topmost_layout'].unlock ('main')

        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('voting_protocol_common.active_tab') || 'voting_protocol_common_log'

        data.__read_only = 1

        if ($_USER.role.admin) data.item.org_label = data.item ['vc_orgs.label']

        data.item.err_text = data.item ['out_soap.err_text']

        console.log (data)

        done (data)
        
    }

})